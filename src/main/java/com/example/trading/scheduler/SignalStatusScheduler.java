package com.example.trading.scheduler;

import com.example.trading.domain.SignalStatus;
import com.example.trading.domain.TradingSignal;
import com.example.trading.repository.TradingSignalRepository;
import com.example.trading.service.TradingSignalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(SignalStatusScheduler.class);

    private final TradingSignalRepository repository;
    private final TradingSignalService service;

    public SignalStatusScheduler(TradingSignalRepository repository,
                                 TradingSignalService service) {
        this.repository = repository;
        this.service = service;
    }

    /**
     * Runs every 1 minute.
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 * * * * *")
    public void refreshOpenSignals() {
        log.info("Running scheduled signal status refresh...");

        List<TradingSignal> openSignals = repository.findByStatus(SignalStatus.OPEN);

        if (openSignals.isEmpty()) {
            log.info("No OPEN signals to refresh");
            return;
        }

        log.info("Refreshing {} open signals", openSignals.size());

        openSignals.forEach(signal -> {
            try {
                service.getSignalStatus(signal.getId());
            } catch (Exception ex) {
                log.error("Failed to refresh signal {}: {}", signal.getId(), ex.getMessage());
            }
        });
    }
}

