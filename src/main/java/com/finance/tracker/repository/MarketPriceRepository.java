package com.finance.tracker.repository;

import com.finance.tracker.domain.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
    Optional<MarketPrice> findFirstBySymbolAndExchangeOrderByTimestampDesc(String symbol, String exchange);
}
