package com.example.trading.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class BinanceClient {

    private final WebClient webClient;

    public BinanceClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://api.binance.com")
                .build();
    }

    public BigDecimal getCurrentPrice(String symbol) {
        return webClient.get()
                .uri("/api/v3/ticker/price?symbol={symbol}", symbol)
                .retrieve()
                .bodyToMono(BinanceTickerPriceResponse.class)
                .map(r -> new BigDecimal(r.getPrice()))
                .block();
    }

    public static class BinanceTickerPriceResponse {
        private String symbol;
        private String price;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
    }
}

