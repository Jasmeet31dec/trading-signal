package com.example.trading.service;

import com.example.trading.domain.Direction;
import com.example.trading.domain.SignalStatus;
import com.example.trading.domain.TradingSignal;
import com.example.trading.dto.TradingSignalRequest;
import com.example.trading.dto.TradingSignalResponse;
import com.example.trading.integration.BinanceClient;
import com.example.trading.mapper.TradingSignalMapper;
import com.example.trading.repository.TradingSignalRepository;
import com.example.trading.exception.NotFoundException;
import com.example.trading.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradingSignalService {

    private final TradingSignalRepository repository;
    private final BinanceClient binanceClient;

    public TradingSignalService(TradingSignalRepository repository,
                                BinanceClient binanceClient) {
        this.repository = repository;
        this.binanceClient = binanceClient;
    }

    @Transactional
    public TradingSignalResponse createSignal(TradingSignalRequest request) {
        validateTimes(request);
        validatePrices(request);

        TradingSignal entity = TradingSignalMapper.toEntity(request);
        entity.setStatus(SignalStatus.OPEN);

        TradingSignal saved = repository.save(entity);
        return TradingSignalMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TradingSignalResponse> getAllSignals() {
        return repository.findAll()
                .stream()
                .map(TradingSignalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TradingSignalResponse getSignalById(Long id) {
        TradingSignal signal = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Signal not found: " + id));
        return TradingSignalMapper.toResponse(signal);
    }

    @Transactional
    public void deleteSignal(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Signal not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public TradingSignalResponse getSignalStatus(Long id) {
        TradingSignal signal = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Signal not found: " + id));

        updateStatusIfNeeded(signal);

        TradingSignal saved = repository.save(signal);
        return TradingSignalMapper.toResponse(saved);
    }

    private void validateTimes(TradingSignalRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        if (request.getExpiryTime().isBefore(request.getEntryTime())) {
            throw new BadRequestException("Expiry time must be after entry time");
        }

        Duration diff = Duration.between(request.getEntryTime(), now);
        if (diff.isNegative() || diff.toHours() > 24) {
            throw new BadRequestException("Entry time may be up to 24 hours in the past");
        }
    }

    private void validatePrices(TradingSignalRequest request) {
        BigDecimal entry = request.getEntryPrice();
        BigDecimal stop = request.getStopLoss();
        BigDecimal target = request.getTargetPrice();

        if (request.getDirection() == Direction.BUY) {
            if (!(stop.compareTo(entry) < 0 && target.compareTo(entry) > 0)) {
                throw new BadRequestException("BUY: StopLoss < EntryPrice and TargetPrice > EntryPrice required");
            }
        } else if (request.getDirection() == Direction.SELL) {
            if (!(stop.compareTo(entry) > 0 && target.compareTo(entry) < 0)) {
                throw new BadRequestException("SELL: StopLoss > EntryPrice and TargetPrice < EntryPrice required");
            }
        }
    }

    private void updateStatusIfNeeded(TradingSignal signal) {
        // Final states never change
        if (signal.getStatus() == SignalStatus.TARGET_HIT
                || signal.getStatus() == SignalStatus.STOPLOSS_HIT
                || signal.getStatus() == SignalStatus.EXPIRED) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal currentPrice = binanceClient.getCurrentPrice(signal.getSymbol());

        Direction dir = signal.getDirection();
        BigDecimal entry = signal.getEntryPrice();
        BigDecimal stop = signal.getStopLoss();
        BigDecimal target = signal.getTargetPrice();

        // Price-based status
        if (dir == Direction.BUY) {
            if (currentPrice.compareTo(target) >= 0) {
                signal.setStatus(SignalStatus.TARGET_HIT);
                signal.setRealizedRoi(calculateRoi(dir, entry, currentPrice));
                return;
            }
            if (currentPrice.compareTo(stop) <= 0) {
                signal.setStatus(SignalStatus.STOPLOSS_HIT);
                signal.setRealizedRoi(calculateRoi(dir, entry, currentPrice));
                return;
            }
        } else { // SELL
            if (currentPrice.compareTo(target) <= 0) {
                signal.setStatus(SignalStatus.TARGET_HIT);
                signal.setRealizedRoi(calculateRoi(dir, entry, currentPrice));
                return;
            }
            if (currentPrice.compareTo(stop) >= 0) {
                signal.setStatus(SignalStatus.STOPLOSS_HIT);
                signal.setRealizedRoi(calculateRoi(dir, entry, currentPrice));
                return;
            }
        }

        // Expiry rule
        if (now.isAfter(signal.getExpiryTime())) {
            signal.setStatus(SignalStatus.EXPIRED);
            // realizedRoi stays null
        }
    }

    private BigDecimal calculateRoi(Direction direction,
                                    BigDecimal entry,
                                    BigDecimal current) {
        BigDecimal roi;
        if (direction == Direction.BUY) {
            roi = current.subtract(entry)
                    .divide(entry, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            roi = entry.subtract(current)
                    .divide(entry, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return roi.setScale(2, RoundingMode.HALF_UP);
    }
}

