package com.expensetracker.controller;

import com.expensetracker.dto.RecurringExpenseRequest;
import com.expensetracker.dto.RecurringExpenseResponse;
import com.expensetracker.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-expenses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecurringExpenseController {
    
    private final RecurringExpenseService recurringExpenseService;
    
    @GetMapping
    public ResponseEntity<List<RecurringExpenseResponse>> getAllRecurringExpenses() {
        log.info("GET /api/recurring-expenses - Fetching all recurring expenses");
        List<RecurringExpenseResponse> expenses = recurringExpenseService.getAllRecurringExpenses();
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<RecurringExpenseResponse>> getActiveRecurringExpenses() {
        log.info("GET /api/recurring-expenses/active - Fetching active recurring expenses");
        List<RecurringExpenseResponse> expenses = recurringExpenseService.getActiveRecurringExpenses();
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecurringExpenseResponse> getRecurringExpenseById(@PathVariable Long id) {
        log.info("GET /api/recurring-expenses/{} - Fetching recurring expense by id", id);
        RecurringExpenseResponse expense = recurringExpenseService.getRecurringExpenseById(id);
        return ResponseEntity.ok(expense);
    }
    
    @PostMapping
    public ResponseEntity<RecurringExpenseResponse> createRecurringExpense(@Valid @RequestBody RecurringExpenseRequest request) {
        log.info("POST /api/recurring-expenses - Creating new recurring expense");
        RecurringExpenseResponse expense = recurringExpenseService.createRecurringExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RecurringExpenseResponse> updateRecurringExpense(
            @PathVariable Long id,
            @Valid @RequestBody RecurringExpenseRequest request) {
        log.info("PUT /api/recurring-expenses/{} - Updating recurring expense", id);
        RecurringExpenseResponse expense = recurringExpenseService.updateRecurringExpense(id, request);
        return ResponseEntity.ok(expense);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringExpense(@PathVariable Long id) {
        log.info("DELETE /api/recurring-expenses/{} - Deleting recurring expense", id);
        recurringExpenseService.deleteRecurringExpense(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleRecurringExpense(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("PATCH /api/recurring-expenses/{}/toggle - Setting active to {}", id, active);
        recurringExpenseService.toggleRecurringExpense(id, active);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/process")
    public ResponseEntity<Void> processRecurringExpenses() {
        log.info("POST /api/recurring-expenses/process - Manually triggering recurring expense processing");
        recurringExpenseService.processRecurringExpenses();
        return ResponseEntity.ok().build();
    }
}
