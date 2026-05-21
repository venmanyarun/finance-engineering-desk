package com.finance.tracker.repository;

import com.finance.tracker.domain.RecurringObligation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringObligationRepository extends JpaRepository<RecurringObligation, Long> {
    List<RecurringObligation> findByUserId(Long userId);
}