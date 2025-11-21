package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.instrument.CandlestickGranularity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ForexController {

    // 1. SZÁMLA INFORMÁCIÓK
    @GetMapping("/account_info")
    public String getAccountInfo(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("summary", summary);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba: " + e.getMessage());
        }
        return "account_info";
    }

    // 2. AKTUÁLIS ÁR (Forex-AktÁr)
    @GetMapping("/actual_prices")
    public String showActualPriceForm() {
        return "actual_prices";
    }

    @PostMapping("/actual_prices")
    public String getActualPrice(@RequestParam String instrument, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            List<String> instruments = new ArrayList<>();
            instruments.add(instrument);

            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = ctx.pricing.get(request);

            if (!resp.getPrices().isEmpty()) {
                model.addAttribute("priceObj", resp.getPrices().get(0));
            }
            model.addAttribute("selectedInstrument", instrument);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba: " + e.getMessage());
        }
        return "actual_prices";
    }

    // 3. HISTORIKUS ÁR (Forex-HistÁr)
    @GetMapping("/hist_prices")
    public String showHistPriceForm() {
        return "hist_prices";
    }

    @PostMapping("/hist_prices")
    public String getHistPrice(@RequestParam String instrument,
                               @RequestParam String granularity,
                               Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(instrument));
            request.setCount(10L); // Utolsó 10 adatot kérjük
            request.setGranularity(CandlestickGranularity.valueOf(granularity));

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);

            model.addAttribute("candles", resp.getCandles());
            model.addAttribute("selectedInstrument", instrument);
            model.addAttribute("selectedGranularity", granularity);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba: " + e.getMessage());
        }
        return "hist_prices";
    }
}