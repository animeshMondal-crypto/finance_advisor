package com.krypto.financeadvisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FinanceadvisorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceadvisorApplication.class, args);
	}

}
