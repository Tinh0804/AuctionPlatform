package com.ecommerce.auctionplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AuctionplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuctionplatformApplication.class, args);
	}

}
