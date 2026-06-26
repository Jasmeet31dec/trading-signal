package com.example.trading;

import org.springframework.boot.SpringApplication;

public class TestTradingApplication {

	public static void main(String[] args) {
		SpringApplication.from(TradingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
