package com.finance.tracker.service;

import com.finance.tracker.domain.MarketPrice;
import com.finance.tracker.repository.MarketPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MarketDataServiceImpl implements MarketDataService {
    @Autowired private MarketPriceRepository priceRepository;

    @Override
    public Optional<BigDecimal> getLatestPrice(String symbol, String exchange) {
        return priceRepository.findFirstBySymbolAndExchangeOrderByTimestampDesc(symbol, exchange)
                .map(MarketPrice::getPrice);
    }

    @Override
    public MarketPrice snapshotPrice(String symbol, String exchange, BigDecimal price, String source) {
        MarketPrice p = new MarketPrice();
        p.setSymbol(symbol);
        p.setExchange(exchange);
        p.setPrice(price);
        p.setSource(source);
        p.setTimestamp(LocalDateTime.now());
        return priceRepository.save(p);
    }
}
