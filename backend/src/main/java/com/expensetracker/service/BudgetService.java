package com.expensetracker.service;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.dto.BudgetStatusResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Budget;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    
    @Transactional(readOnly = true)
    public BudgetResponse getBudget() {
        log.debug("Fetching current budget");
        Budget budget = budgetRepository.findFirstByOrderByUpdatedAtDesc()
            .orElseThrow(() -> new ResourceNotFoundException("No budget has been set"));
        return BudgetResponse.fromEntity(budget);
    }
    
    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        log.info("Setting budget to: {}", request.getMonthlyLimit());
        
        // Get existing budget or create new one
        Budget budget = budgetRepository.findFirstByOrderByUpdatedAtDesc()
            .orElse(new Budget());
        
        budget.setMonthlyLimit(request.getMonthlyLimit());
        
        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget saved with id: {}", savedBudget.getId());
        return BudgetResponse.fromEntity(savedBudget);
    }
    
    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus() {
        log.debug("Fetching budget status");
        
        // Get current budget
        Budget budget = budgetRepository.findFirstByOrderByUpdatedAtDesc()
            .orElseThrow(() -> new ResourceNotFoundException("No budget has been set"));
        
        // Get current month's total spending
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        BigDecimal totalSpent = expenseRepository.findTotalAmountBetweenDates(startDate, endDate);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        
        // Calculate remaining and percentage
        BigDecimal monthlyLimit = budget.getMonthlyLimit();
        BigDecimal remaining = monthlyLimit.subtract(totalSpent);
        
        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (monthlyLimit.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = totalSpent
                .divide(monthlyLimit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Determine status
        BudgetStatusResponse.BudgetStatus status;
        if (percentageUsed.compareTo(new BigDecimal("100")) >= 0) {
            status = BudgetStatusResponse.BudgetStatus.OVER_BUDGET;
        } else if (percentageUsed.compareTo(new BigDecimal("80")) >= 0) {
            status = BudgetStatusResponse.BudgetStatus.WARNING;
        } else {
            status = BudgetStatusResponse.BudgetStatus.HEALTHY;
        }
        
        return new BudgetStatusResponse(
            monthlyLimit,
            totalSpent,
            remaining,
            percentageUsed,
            status
        );
    }
}
