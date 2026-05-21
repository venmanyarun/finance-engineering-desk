package com.finance.tracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataExportImportService {

    @Autowired private FinancialAccountRepository accountRepository;
    @Autowired private IncomeSourceRepository incomeRepository;
    @Autowired private RecurringObligationRepository obligationRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    public String exportDataToJson() throws IOException {
        Long userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("accounts", accountRepository.findByUserId(userId));
        data.put("income", incomeRepository.findByUserId(userId));
        data.put("obligations", obligationRepository.findByUserId(userId));
        data.put("transactions", transactionRepository.findByUserIdOrderByTransactionDateDesc(userId));
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }

    public void importDataFromJson(String json) throws IOException {
        Long userId = getCurrentUserId();
        Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        
        if (data.containsKey("accounts")) {
            Iterable<FinancialAccount> items = objectMapper.convertValue(data.get("accounts"), new TypeReference<Iterable<FinancialAccount>>() {});
            items.forEach(i -> i.setUserId(userId));
            accountRepository.saveAll(items);
        }

        if (data.containsKey("income")) {
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
    }
}