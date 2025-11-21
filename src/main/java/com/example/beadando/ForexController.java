package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForexController {

    @GetMapping("/account_info")
    public String getAccountInfo(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);

        try {
        AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();

         model.addAttribute("summary", summary);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba a kapcsolódáskor: " + e.getMessage());
        }

        return "account_info";
    }
}