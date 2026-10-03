package com.finance.tracker.service;

import com.finance.tracker.domain.RecurringObligation;
import com.finance.tracker.repository.FinancialAccountRepository;
import com.finance.tracker.repository.IncomeSourceRepository;
import com.finance.tracker.repository.RecurringObligationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

    @Mock
    private IncomeSourceRepository incomeRepository;

    @Mock
    private RecurringObligationRepository obligationRepository;

    @Mock
    private FinancialAccountRepository accountRepository;

    @InjectMocks
    private ProjectionService projectionService;

    @Test
    void includesAnnualObligationDueThisMonthWhenStoredDateIsLastYear() {
        Long userId = 1L;
        RecurringObligation premium = new RecurringObligation();
        premium.setNextDueDate(LocalDate.now().minusYears(1));
        premium.setFrequency(RecurringObligation.PaymentFrequency.YEARLY);
        premium.setAmount(new BigDecimal("325000"));

        when(incomeRepository.findByUserId(userId)).thenReturn(List.of());
        when(obligationRepository.findByUserId(userId)).thenReturn(List.of(premium));

        BigDecimal expectedSpend = projectionService.getCurrentMonthExpectedSpend(userId);

        assertEquals(new BigDecimal("325000"), expectedSpend);
    }
}