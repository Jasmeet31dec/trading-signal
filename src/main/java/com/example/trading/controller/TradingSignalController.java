package com.example.trading.controller;

import com.example.trading.dto.TradingSignalRequest;
import com.example.trading.dto.TradingSignalResponse;
import com.example.trading.service.TradingSignalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signals")
public class TradingSignalController {

    private final TradingSignalService service;

    public TradingSignalController(TradingSignalService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradingSignalResponse createSignal(@Valid @RequestBody TradingSignalRequest request) {
        return service.createSignal(request);
    }

    @GetMapping
    public List<TradingSignalResponse> getAllSignals() {
        return service.getAllSignals();
    }

    @GetMapping("/{id}")
    public TradingSignalResponse getSignalById(@PathVariable Long id) {
        return service.getSignalById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSignal(@PathVariable Long id) {
        service.deleteSignal(id);
    }

    @GetMapping("/{id}/status")
    public TradingSignalResponse getSignalStatus(@PathVariable Long id) {
        return service.getSignalStatus(id);
    }
}

