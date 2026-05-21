package com.finance.tracker.service;

import com.finance.tracker.domain.MarketPrice;
import java.math.BigDecimal;
import java.util.Optional;

public interface MarketDataService {
    Optional<BigDecimal> getLatestPrice(String symbol, String exchange);
    MarketPrice snapshotPrice(String symbol, String exchange, BigDecimal price, String source);
}
