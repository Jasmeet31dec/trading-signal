package com.example.trading.mapper;

import com.example.trading.domain.TradingSignal;
import com.example.trading.dto.TradingSignalRequest;
import com.example.trading.dto.TradingSignalResponse;
import org.springframework.stereotype.Component;

@Component
public class TradingSignalMapper {

    public static TradingSignal toEntity(TradingSignalRequest dto) {
        TradingSignal entity = new TradingSignal();
        entity.setSymbol(dto.getSymbol());
        entity.setDirection(dto.getDirection());
        entity.setEntryPrice(dto.getEntryPrice());
        entity.setStopLoss(dto.getStopLoss());
        entity.setTargetPrice(dto.getTargetPrice());
        entity.setEntryTime(dto.getEntryTime());
        entity.setExpiryTime(dto.getExpiryTime());
        return entity;
    }

    public static TradingSignalResponse toResponse(TradingSignal entity) {
        TradingSignalResponse resp = new TradingSignalResponse();
        resp.setId(entity.getId());
        resp.setSymbol(entity.getSymbol());
        resp.setDirection(entity.getDirection());
        resp.setEntryPrice(entity.getEntryPrice());
        resp.setStopLoss(entity.getStopLoss());
        resp.setTargetPrice(entity.getTargetPrice());
        resp.setEntryTime(entity.getEntryTime());
        resp.setExpiryTime(entity.getExpiryTime());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setStatus(entity.getStatus());
        resp.setRealizedRoi(entity.getRealizedRoi());
        return resp;
    }
}

