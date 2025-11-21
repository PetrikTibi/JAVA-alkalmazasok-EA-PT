package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeSpecifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ForexController {

    private Context getContext() {
        return new Context(Config.URL, Config.TOKEN);
    }

    // --- 1. SZÁMLA ---
    @GetMapping("/account_info")
    public String getAccountInfo(Model model) {
        try {
            AccountSummary summary = getContext().account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("summary", summary);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "account_info";
    }

    // --- 2. AKTUÁLIS ÁR ---
    @GetMapping("/actual_prices")
    public String showActualPriceForm() { return "actual_prices"; }

    @PostMapping("/actual_prices")
    public String getActualPrice(@RequestParam String instrument, Model model) {
        try {
            List<String> instruments = new ArrayList<>();
            instruments.add(instrument);
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = getContext().pricing.get(request);
            if (!resp.getPrices().isEmpty()) model.addAttribute("priceObj", resp.getPrices().get(0));
            model.addAttribute("selectedInstrument", instrument);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "actual_prices";
    }

    // --- 3. HISTORIKUS ÁR ---
    @GetMapping("/hist_prices")
    public String showHistPriceForm() { return "hist_prices"; }

    @PostMapping("/hist_prices")
    public String getHistPrice(@RequestParam String instrument, @RequestParam String granularity, Model model) {
        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(instrument));
            request.setCount(10L);
            request.setGranularity(CandlestickGranularity.valueOf(granularity));
            InstrumentCandlesResponse resp = getContext().instrument.candles(request);
            model.addAttribute("candles", resp.getCandles());
            model.addAttribute("selectedInstrument", instrument);
            model.addAttribute("selectedGranularity", granularity);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "hist_prices";
    }

    // --- 4. POZÍCIÓ NYITÁS ---
    @GetMapping("/open_position")
    public String showOpenPositionForm() { return "open_position"; }

    @PostMapping("/open_position")
    public String openPosition(@RequestParam String instrument, @RequestParam Integer units, Model model) {
        try {
            // Összeállítjuk a kérést
            OrderCreateRequest request = new OrderCreateRequest(Config.ACCOUNTID);
            MarketOrderRequest marketOrder = new MarketOrderRequest();
            marketOrder.setInstrument(new InstrumentName(instrument));
            marketOrder.setUnits(units); // Pozitív = Vétel, Negatív = Eladás
            request.setOrder(marketOrder);

            // lekérés
            OrderCreateResponse response = getContext().order.create(request);

            // eredmény a kérésre
            String tradeId = response.getOrderFillTransaction().getId().toString();
            model.addAttribute("message", "Sikeres nyitás! Trade ID: " + tradeId);
            model.addAttribute("openedInstrument", instrument);
            model.addAttribute("openedUnits", units);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba a nyitásnál: " + e.getMessage());
        }
        return "open_position";
    }

    // --- 5. NYITOTT POZÍCIÓK LISTÁZÁSA ---
    @GetMapping("/positions")
    public String listPositions(Model model) {
        try {
            List<Trade> trades = getContext().trade.listOpen(Config.ACCOUNTID).getTrades();
            model.addAttribute("trades", trades);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "positions";
    }

    // --- 6. POZÍCIÓ ZÁRÁS ---
    @GetMapping("/close_position")
    public String showClosePositionForm() { return "close_position"; }

    @PostMapping("/close_position")
    public String closePosition(@RequestParam String tradeId, Model model) {
        try {
            // Lezárjuk a megadott azonosítójú kereskedést
            getContext().trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(tradeId)));
            model.addAttribute("message", "Sikeres zárás! Trade ID: " + tradeId);
        } catch (Exception e) {
            model.addAttribute("error", "Hiba a zárásnál: " + e.getMessage());
        }
        return "close_position";
    }
}