package com.example.beadando;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import soapclient.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class SoapController {

    @GetMapping("/soap")
    public String showSoapForm() {
        return "soap";
    }

    @PostMapping("/soap")
    public String getRates(@RequestParam String startDate,
                           @RequestParam String endDate,
                           @RequestParam String currency,
                           Model model) {
        try {
            // 1. Lekérdezés az MNB-től
            MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
            MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();
            String xmlResult = service.getExchangeRates(startDate, endDate, currency);

            // 2. XML Feldolgozása (Dátumok és Értékek kinyerése)
            List<String> dates = new ArrayList<>();
            List<Double> rates = new ArrayList<>();

            // XML elemző létrehozása
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResult)));

            // Napok (Day) bejárása
            NodeList days = doc.getElementsByTagName("Day");
            for (int i = 0; i < days.getLength(); i++) {
                Element dayNode = (Element) days.item(i);
                String date = dayNode.getAttribute("date");

                // Árfolyam (Rate) keresése a napon belül
                NodeList rateNodes = dayNode.getElementsByTagName("Rate");
                if (rateNodes.getLength() > 0) {
                    String rateStr = rateNodes.item(0).getTextContent();
                    // A magyar tizedesvesszőt (,) pontra (.) cseréljük
                    Double rateVal = Double.parseDouble(rateStr.replace(",", "."));

                    dates.add(date);
                    rates.add(rateVal);
                }
            }

            // Mivel az MNB fordítva adja (legfrissebb elöl), megfordítjuk a listákat a grafikonhoz
            Collections.reverse(dates);
            Collections.reverse(rates);

            // 3. Adatok átadása a HTML-nek
            model.addAttribute("mnbResult", "Sikeres lekérdezés: " + dates.size() + " db adat.");
            model.addAttribute("chartDates", dates); // Dátumok a grafikon X tengelyére
            model.addAttribute("chartRates", rates); // Értékek a grafikon Y tengelyére
            model.addAttribute("currency", currency); // Melyik deviza

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mnbResult", "Hiba: " + e.getMessage());
        }

        return "soap";
    }
}