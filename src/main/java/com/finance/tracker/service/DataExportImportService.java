package com.finance.tracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataExportImportService {

    private static final Logger log = LoggerFactory.getLogger(DataExportImportService.class);

    @Autowired private FinancialAccountRepository accountRepository;
    @Autowired private IncomeSourceRepository incomeRepository;
    @Autowired private RecurringObligationRepository obligationRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long getCurrentUserId() {
        log.debug("Attempting to retrieve current user ID.");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        log.debug("Retrieved current user ID: {}", userId);
        return userId;
    }

    public String exportDataToJson() throws IOException {
        log.info("exportDataToJson called.");
        Long userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("accounts", accountRepository.findByUserId(userId));
        data.put("income", incomeRepository.findByUserId(userId));
        data.put("obligations", obligationRepository.findByUserId(userId));
        data.put("transactions", transactionRepository.findByUserIdOrderByTransactionDateDesc(userId));
        String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        log.info("Successfully exported data for user ID: {}", userId);
        return jsonData;
    }

    public void importDataFromJson(String json) throws IOException {
        log.info("importDataFromJson called.");
        Long userId = getCurrentUserId();
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        
        if (data.containsKey("accounts")) {
            log.debug("Importing accounts for user ID: {}", userId);
            Iterable<FinancialAccount> items = objectMapper.convertValue(data.get("accounts"), new TypeReference<Iterable<FinancialAccount>>() {});
            items.forEach(i -> i.setUserId(userId));
            accountRepository.saveAll(items);
        }

        if (data.containsKey("income")) {
            log.debug("Importing income sources for user ID: {}", userId);
            Iterable<IncomeSource> items = objectMapper.convertValue(data.get("income"), new TypeReference<Iterable<IncomeSource>>() {});
            items.forEach(i -> {
                i.setUserId(userId);
                if (i.getDestinationAccount() != null && i.getDestinationAccount().getId() != null) {
                    accountRepository.findById(i.getDestinationAccount().getId()).ifPresent(i::setDestinationAccount);
                }
            });
            incomeRepository.saveAll(items);
        }

        if (data.containsKey("obligations")) {
            log.debug("Importing recurring obligations for user ID: {}", userId);
            Iterable<RecurringObligation> items = objectMapper.convertValue(data.get("obligations"), new TypeReference<Iterable<RecurringObligation>>() {});
            items.forEach(i -> {
                i.setUserId(userId);
                if (i.getLinkedAccount() != null && i.getLinkedAccount().getId() != null) {
                    accountRepository.findById(i.getLinkedAccount().getId()).ifPresent(i::setLinkedAccount);
                }
            });
            obligationRepository.saveAll(items);
        }

        if (data.containsKey("transactions")) {
            log.debug("Importing transactions for user ID: {}", userId);
            Iterable<Transaction> items = objectMapper.convertValue(data.get("transactions"), new TypeReference<Iterable<Transaction>>() {});
            items.forEach(i -> {
                i.setUserId(userId);
                if (i.getSourceAccount() != null && i.getSourceAccount().getId() != null) {
                    accountRepository.findById(i.getSourceAccount().getId()).ifPresent(i::setSourceAccount);
                }
                if (i.getDestinationAccount() != null && i.getDestinationAccount().getId() != null) {
                    accountRepository.findById(i.getDestinationAccount().getId()).ifPresent(i::setDestinationAccount);
                }
            });
            transactionRepository.saveAll(items);
        }
        log.info("Successfully imported data for user ID: {}", userId);
    }
}