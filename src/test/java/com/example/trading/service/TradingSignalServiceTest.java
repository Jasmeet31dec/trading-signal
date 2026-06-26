package com.example.trading.service;

import com.example.trading.domain.Direction;
import com.example.trading.domain.SignalStatus;
import com.example.trading.domain.TradingSignal;
import com.example.trading.dto.TradingSignalRequest;
import com.example.trading.dto.TradingSignalResponse;
import com.example.trading.exception.BadRequestException;
import com.example.trading.exception.NotFoundException;
import com.example.trading.integration.BinanceClient;
import com.example.trading.repository.TradingSignalRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingSignalServiceTest {

    @Mock
    private TradingSignalRepository repository;

    @Mock
    private BinanceClient binanceClient;

    @InjectMocks
    private TradingSignalService service;

    private TradingSignalRequest baseRequest;

    @BeforeEach
    void setup() {
        baseRequest = new TradingSignalRequest();
        baseRequest.setSymbol("BTCUSDT");
        baseRequest.setEntryPrice(new BigDecimal("100"));
        baseRequest.setStopLoss(new BigDecimal("90"));
        baseRequest.setTargetPrice(new BigDecimal("120"));
        baseRequest.setEntryTime(OffsetDateTime.now().minusHours(1));
        baseRequest.setExpiryTime(OffsetDateTime.now().plusHours(5));
    }

    // ---------------------------------------------------------
    // VALIDATION TESTS
    // ---------------------------------------------------------

    @Test
    void testBuyValidationSuccess() {
        baseRequest.setDirection(Direction.BUY);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TradingSignalResponse response = service.createSignal(baseRequest);

        assertEquals(Direction.BUY, response.getDirection());
    }

    @Test
    void testBuyValidationFails() {
        baseRequest.setDirection(Direction.BUY);
        baseRequest.setStopLoss(new BigDecimal("150")); // invalid

        assertThrows(BadRequestException.class,
                () -> service.createSignal(baseRequest));
    }

    @Test
    void testSellValidationSuccess() {
        baseRequest.setDirection(Direction.SELL);
        baseRequest.setStopLoss(new BigDecimal("110"));
        baseRequest.setTargetPrice(new BigDecimal("80"));

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TradingSignalResponse response = service.createSignal(baseRequest);

        assertEquals(Direction.SELL, response.getDirection());
    }

    @Test
    void testSellValidationFails() {
        baseRequest.setDirection(Direction.SELL);
        baseRequest.setTargetPrice(new BigDecimal("150")); // invalid

        assertThrows(BadRequestException.class,
                () -> service.createSignal(baseRequest));
    }

    @Test
    void testEntryTimeValid() {
        baseRequest.setDirection(Direction.BUY);

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.createSignal(baseRequest));
    }

    @Test
    void testEntryTimeTooOld() {
        baseRequest.setDirection(Direction.BUY);
        baseRequest.setEntryTime(OffsetDateTime.now().minusHours(30));

        assertThrows(BadRequestException.class,
                () -> service.createSignal(baseRequest));
    }

    @Test
    void testExpiryBeforeEntry() {
        baseRequest.setDirection(Direction.BUY);
        baseRequest.setExpiryTime(OffsetDateTime.now().minusHours(2));

        assertThrows(BadRequestException.class,
                () -> service.createSignal(baseRequest));
    }

    // ---------------------------------------------------------
    // STATUS LOGIC TESTS
    // ---------------------------------------------------------

    @Test
    void testBuyTargetHit() {
        TradingSignal signal = createOpenSignal(Direction.BUY);

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("130")); // >= target

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(SignalStatus.TARGET_HIT, response.getStatus());
    }

    @Test
    void testBuyStopLossHit() {
        TradingSignal signal = createOpenSignal(Direction.BUY);

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("80")); // <= stopLoss

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(SignalStatus.STOPLOSS_HIT, response.getStatus());
    }

    @Test
    void testSellTargetHit() {
        TradingSignal signal = createOpenSignal(Direction.SELL);
        signal.setStopLoss(new BigDecimal("110"));
        signal.setTargetPrice(new BigDecimal("80"));

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("75")); // <= target

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(SignalStatus.TARGET_HIT, response.getStatus());
    }

    @Test
    void testSellStopLossHit() {
        TradingSignal signal = createOpenSignal(Direction.SELL);
        signal.setStopLoss(new BigDecimal("110"));
        signal.setTargetPrice(new BigDecimal("80"));

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("120")); // >= stopLoss

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(SignalStatus.STOPLOSS_HIT, response.getStatus());
    }

    @Test
    void testExpiredSignal() {
        TradingSignal signal = createOpenSignal(Direction.BUY);
        signal.setExpiryTime(OffsetDateTime.now().minusMinutes(10)); // expired

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice(any())).thenReturn(new BigDecimal("100"));

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(SignalStatus.EXPIRED, response.getStatus());
    }

    // ---------------------------------------------------------
    // ROI TESTS
    // ---------------------------------------------------------

    @Test
    void testBuyRoiCalculation() {
        TradingSignal signal = createOpenSignal(Direction.BUY);

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("120"));

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(new BigDecimal("20.00"), response.getRealizedRoi());
    }

    @Test
    void testSellRoiCalculation() {
        TradingSignal signal = createOpenSignal(Direction.SELL);
        signal.setStopLoss(new BigDecimal("110"));
        signal.setTargetPrice(new BigDecimal("80"));

        when(repository.findById(1L)).thenReturn(Optional.of(signal));
        when(binanceClient.getCurrentPrice("BTCUSDT"))
                .thenReturn(new BigDecimal("80"));

        TradingSignalResponse response = service.getSignalStatus(1L);

        assertEquals(new BigDecimal("20.00"), response.getRealizedRoi());
    }

    // ---------------------------------------------------------
    // HELPER
    // ---------------------------------------------------------

    private TradingSignal createOpenSignal(Direction direction) {
        TradingSignal s = new TradingSignal();
        s.setId(1L);
        s.setSymbol("BTCUSDT");
        s.setDirection(direction);
        s.setEntryPrice(new BigDecimal("100"));
        s.setStopLoss(new BigDecimal("90"));
        s.setTargetPrice(new BigDecimal("120"));
        s.setEntryTime(OffsetDateTime.now().minusHours(1));
        s.setExpiryTime(OffsetDateTime.now().plusHours(5));
        s.setStatus(SignalStatus.OPEN);
        return s;
    }
}
