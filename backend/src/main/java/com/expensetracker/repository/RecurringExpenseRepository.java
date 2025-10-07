package com.expensetracker.repository;

import com.expensetracker.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    
    List<RecurringExpense> findByActiveTrue();
    
    List<RecurringExpense> findByActiveTrueAndNextOccurrenceLessThanEqual(LocalDate date);
}
