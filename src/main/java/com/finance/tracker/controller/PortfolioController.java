package com.finance.tracker.controller;

import com.finance.tracker.domain.Holding;
import com.finance.tracker.domain.MarketPrice;
import com.finance.tracker.domain.Portfolio;
import com.finance.tracker.repository.HoldingRepository;
import com.finance.tracker.repository.PortfolioRepository;
import com.finance.tracker.service.MarketDataService;
import com.finance.tracker.repository.MarketPriceRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "http://localhost:3000")
public class PortfolioController {

    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private HoldingRepository holdingRepository;
    @Autowired private MarketDataService marketDataService;
    @Autowired private MarketPriceRepository marketPriceRepository;
    @Autowired private UserRepository userRepository;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    @GetMapping("/portfolios")
    public List<Portfolio> listPortfolios() {
        try {
            Long userId = getCurrentUserId();
            return portfolioRepository.findByUserId(userId);
        } catch (Exception e) {
            // If authentication or user lookup fails, return empty list to avoid 500 errors in UI during development
            return java.util.Collections.emptyList();
        }
    }

    @PostMapping("/portfolios")
    public ResponseEntity<?> createPortfolio(@RequestBody Portfolio p) {
        try {
            p.setUserId(getCurrentUserId());
            Portfolio saved = portfolioRepository.save(p);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            // Authentication or DB failure - return an informative status
            return ResponseEntity.status(401).body("Unable to create portfolio: authentication required");
        }
    }

    @GetMapping("/portfolios/{id}/holdings")
    public List<Holding> listHoldings(@PathVariable Long id) {
        return holdingRepository.findByPortfolioId(id);
    }

    @PostMapping("/portfolios/{id}/holdings")
    public Holding addHolding(@PathVariable Long id, @RequestBody Holding h) {
        portfolioRepository.findById(id).ifPresent(h::setPortfolio);
        h.setUserId(getCurrentUserId());
        return holdingRepository.save(h);
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<?> deleteHolding(@PathVariable Long id) {
        try {
            if (!holdingRepository.existsById(id)) {
                return ResponseEntity.status(404).body("Holding not found");
            }
            holdingRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            return ResponseEntity.status(404).body("Holding not found");
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to delete holding");
        }
    }

    @DeleteMapping("/portfolios/{id}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            // verify ownership
            Portfolio p = portfolioRepository.findById(id).orElse(null);
            if (p == null || !p.getUserId().equals(userId)) {
                return ResponseEntity.status(404).body("Portfolio not found or access denied");
            }
            // remove holdings
            List<Holding> holdings = holdingRepository.findByPortfolioId(id);
            if (holdings != null && !holdings.isEmpty()) {
                holdingRepository.deleteAll(holdings);
            }
            portfolioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to delete portfolio");
        }
    }

    @PostMapping("/market-prices/snapshot")
    public MarketPrice snapshot(@RequestBody MarketPrice p) {
        if (p.getTimestamp() == null) p.setTimestamp(java.time.LocalDateTime.now());
        return marketPriceRepository.save(p);
    }

    @GetMapping("/market-prices")
    public ResponseEntity<MarketPrice> getLatestPrice(@RequestParam String symbol, @RequestParam(defaultValue = "") String exchange) {
        return marketPriceRepository.findFirstBySymbolAndExchangeOrderByTimestampDesc(symbol, exchange)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/market-prices/import")
    public ResponseEntity<?> importPricesCsv(@RequestBody String csv) {
        // Accept CSV with header: symbol,exchange,price,timestamp(optional ISO)
        String[] lines = csv.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) continue;
            if (i == 0 && line.toLowerCase().contains("symbol") && line.toLowerCase().contains("price")) continue;
            String[] parts = line.split(",");
            try {
                String symbol = parts.length > 0 ? parts[0].trim() : null;
                String exchange = parts.length > 1 ? parts[1].trim() : "";
                java.math.BigDecimal price = parts.length > 2 ? new java.math.BigDecimal(parts[2].trim()) : java.math.BigDecimal.ZERO;
                java.time.LocalDateTime ts = parts.length > 3 && !parts[3].trim().isEmpty() ? java.time.LocalDateTime.parse(parts[3].trim()) : java.time.LocalDateTime.now();
                MarketPrice p = new MarketPrice();
                p.setSymbol(symbol);
                p.setExchange(exchange);
                p.setPrice(price);
                p.setTimestamp(ts);
                marketPriceRepository.save(p);
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/market-prices/import-file")
    public ResponseEntity<?> importPricesFile(@RequestParam("file") MultipartFile file) {
        try {
            String csv = new String(file.getBytes());
            return importPricesCsv(csv);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to read file");
        }
    }
}
