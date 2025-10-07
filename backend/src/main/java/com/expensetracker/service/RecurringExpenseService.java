package com.expensetracker.service;

import com.expensetracker.dto.RecurringExpenseRequest;
import com.expensetracker.dto.RecurringExpenseResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.model.RecurringExpense;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.RecurringExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringExpenseService {
    
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    
    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> getAllRecurringExpenses() {
        log.debug("Fetching all recurring expenses");
        return recurringExpenseRepository.findAll().stream()
            .map(RecurringExpenseResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> getActiveRecurringExpenses() {
        log.debug("Fetching active recurring expenses");
        return recurringExpenseRepository.findByActiveTrue().stream()
            .map(RecurringExpenseResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RecurringExpenseResponse getRecurringExpenseById(Long id) {
        log.debug("Fetching recurring expense with id: {}", id);
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found with id: " + id));
        return RecurringExpenseResponse.fromEntity(recurringExpense);
    }
    
    @Transactional
    public RecurringExpenseResponse createRecurringExpense(RecurringExpenseRequest request) {
        log.info("Creating new recurring expense: {}", request);
        RecurringExpense recurringExpense = new RecurringExpense();
        recurringExpense.setAmount(request.getAmount());
        recurringExpense.setCategory(request.getCategory());
        recurringExpense.setDescription(request.getDescription());
        recurringExpense.setFrequency(request.getFrequency());
        recurringExpense.setStartDate(request.getStartDate());
        recurringExpense.setEndDate(request.getEndDate());
        recurringExpense.setNextOccurrence(request.getStartDate());
        recurringExpense.setActive(true);
        
        RecurringExpense saved = recurringExpenseRepository.save(recurringExpense);
        log.info("Created recurring expense with id: {}", saved.getId());
        return RecurringExpenseResponse.fromEntity(saved);
    }
    
    @Transactional
    public RecurringExpenseResponse updateRecurringExpense(Long id, RecurringExpenseRequest request) {
        log.info("Updating recurring expense with id: {}", id);
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found with id: " + id));
        
        recurringExpense.setAmount(request.getAmount());
        recurringExpense.setCategory(request.getCategory());
        recurringExpense.setDescription(request.getDescription());
        recurringExpense.setFrequency(request.getFrequency());
        recurringExpense.setStartDate(request.getStartDate());
        recurringExpense.setEndDate(request.getEndDate());
        
        RecurringExpense updated = recurringExpenseRepository.save(recurringExpense);
        log.info("Updated recurring expense with id: {}", id);
        return RecurringExpenseResponse.fromEntity(updated);
    }
    
    @Transactional
    public void deleteRecurringExpense(Long id) {
        log.info("Deleting recurring expense with id: {}", id);
        if (!recurringExpenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurring expense not found with id: " + id);
        }
        recurringExpenseRepository.deleteById(id);
        log.info("Deleted recurring expense with id: {}", id);
    }
    
    @Transactional
    public void toggleRecurringExpense(Long id, boolean active) {
        log.info("Toggling recurring expense {} to active: {}", id, active);
        RecurringExpense recurringExpense = recurringExpenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring expense not found with id: " + id));
        recurringExpense.setActive(active);
        recurringExpenseRepository.save(recurringExpense);
    }
    
    @Scheduled(cron = "0 0 1 * * *") // Run daily at 1 AM
    @Transactional
    public void processRecurringExpenses() {
        log.info("Processing recurring expenses");
        LocalDate today = LocalDate.now();
        
        List<RecurringExpense> dueExpenses = recurringExpenseRepository
            .findByActiveTrueAndNextOccurrenceLessThanEqual(today);
        
        for (RecurringExpense recurring : dueExpenses) {
            // Check if end date has passed
            if (recurring.getEndDate() != null && today.isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
                recurringExpenseRepository.save(recurring);
                log.info("Deactivated recurring expense {} - end date reached", recurring.getId());
                continue;
            }
            
            // Create the expense
            Expense expense = new Expense();
            expense.setAmount(recurring.getAmount());
            expense.setCategory(recurring.getCategory());
            expense.setDate(recurring.getNextOccurrence());
            expense.setDescription(recurring.getDescription() + " (Recurring)");
            expenseRepository.save(expense);
            
            log.info("Created expense from recurring expense {}", recurring.getId());
            
            // Calculate next occurrence
            LocalDate nextOccurrence = calculateNextOccurrence(recurring.getNextOccurrence(), recurring.getFrequency());
            recurring.setNextOccurrence(nextOccurrence);
            recurringExpenseRepository.save(recurring);
        }
        
        log.info("Processed {} recurring expenses", dueExpenses.size());
    }
    
    private LocalDate calculateNextOccurrence(LocalDate current, RecurringExpense.RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case BIWEEKLY -> current.plusWeeks(2);
            case MONTHLY -> current.plusMonths(1);
            case QUARTERLY -> current.plusMonths(3);
            case YEARLY -> current.plusYears(1);
        };
    }
}
