package com.example.trading.repository;

import com.example.trading.domain.TradingSignal;
import com.example.trading.domain.SignalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {

    List<TradingSignal> findByStatus(SignalStatus status);
}

