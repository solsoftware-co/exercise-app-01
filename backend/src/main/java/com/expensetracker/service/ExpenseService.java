package com.expensetracker.service;

import com.expensetracker.dto.*;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        log.debug("Fetching all expenses");
        return expenseRepository.findAll().stream()
            .map(ExpenseResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        log.debug("Fetching expense with id: {}", id);
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return ExpenseResponse.fromEntity(expense);
    }
    
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating new expense: {}", request);
        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Created expense with id: {}", savedExpense.getId());
        return ExpenseResponse.fromEntity(savedExpense);
    }
    
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        log.info("Updating expense with id: {}", id);
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        
        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Updated expense with id: {}", id);
        return ExpenseResponse.fromEntity(updatedExpense);
    }
    
    @Transactional
    public void deleteExpense(Long id) {
        log.info("Deleting expense with id: {}", id);
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
        log.info("Deleted expense with id: {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategorySummary() {
        log.debug("Fetching category summary");
        return expenseRepository.findTotalByCategory().stream()
            .map(summary -> new CategorySummaryResponse(
                summary.getCategory(),
                summary.getTotal() != null ? summary.getTotal() : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary() {
        log.debug("Fetching monthly summary");
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        BigDecimal total = expenseRepository.findTotalAmountBetweenDates(startDate, endDate);
        
        return new MonthlySummaryResponse(
            total != null ? total : BigDecimal.ZERO,
            currentMonth.getMonthValue(),
            currentMonth.getYear()
        );
    }
}
