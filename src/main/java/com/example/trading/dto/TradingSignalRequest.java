package com.example.trading.dto;

import com.example.trading.domain.Direction;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TradingSignalRequest {

    @NotBlank
    private String symbol;

    @NotNull
    private Direction direction;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal entryPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal stopLoss;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal targetPrice;

    @NotNull
    private OffsetDateTime entryTime;

    @NotNull
    private OffsetDateTime expiryTime;

    public TradingSignalRequest() {}

    // getters and setters

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public OffsetDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(OffsetDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
}

