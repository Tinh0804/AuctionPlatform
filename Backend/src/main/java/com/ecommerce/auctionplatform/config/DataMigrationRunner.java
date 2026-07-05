package com.ecommerce.auctionplatform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            int updatedAuctions = jdbcTemplate.update("UPDATE auctions SET condition = 'LIKE_NEW' WHERE condition = 'USED'");
            log.info("Migrated {} auctions from USED to LIKE_NEW", updatedAuctions);
        } catch (Exception e) {
            log.warn("Auctions table might not have condition field", e.getMessage());
        }

        try {
            int updatedProducts = jdbcTemplate.update("UPDATE products SET condition = 'LIKE_NEW' WHERE condition = 'USED'");
            log.info("Migrated {} products from USED to LIKE_NEW", updatedProducts);
        } catch (Exception e) {
            log.warn("Products table might not have condition field", e.getMessage());
        }
    }
}
