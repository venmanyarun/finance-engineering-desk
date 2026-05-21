package com.finance.tracker.repository;

import com.finance.tracker.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUserId(Long userId);
    List<Holding> findByPortfolioId(Long portfolioId);
    java.util.Optional<com.finance.tracker.domain.Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
