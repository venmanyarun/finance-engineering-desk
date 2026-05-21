package com.finance.tracker.service;

import com.finance.tracker.domain.Holding;
import com.finance.tracker.repository.HoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PortfolioService {
    @Autowired private HoldingRepository holdingRepository;
    @Autowired private MarketDataService marketDataService;

    public BigDecimal valuePortfolio(Long portfolioId) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        BigDecimal total = BigDecimal.ZERO;
        for (Holding h : holdings) {
            BigDecimal qty = h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO;
            BigDecimal price = marketDataService.getLatestPrice(h.getSymbol(), h.getExchange()).orElse(BigDecimal.ZERO);
            total = total.add(qty.multiply(price));
        }
        return total;
    }
}
