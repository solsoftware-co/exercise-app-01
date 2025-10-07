package com.expensetracker.controller;

import com.expensetracker.dto.*;
import com.expensetracker.model.ExpenseCategory;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        log.info("GET /api/expenses - Fetching all expenses");
        List<ExpenseResponse> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseResponse>> getFilteredExpenses(
            @RequestParam(required = false) List<ExpenseCategory> categories,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/expenses/filter - categories: {}, startDate: {}, endDate: {}", categories, startDate, endDate);
        List<ExpenseResponse> expenses = expenseService.getFilteredExpenses(categories, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        log.info("GET /api/expenses/{} - Fetching expense by id", id);
        ExpenseResponse expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }
    
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        log.info("POST /api/expenses - Creating new expense");
        ExpenseResponse expense = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        log.info("PUT /api/expenses/{} - Updating expense", id);
        ExpenseResponse expense = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(expense);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        log.info("DELETE /api/expenses/{} - Deleting expense", id);
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/summary/by-category")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary() {
        log.info("GET /api/expenses/summary/by-category - Fetching category summary");
        List<CategorySummaryResponse> summary = expenseService.getCategorySummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/summary/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary() {
        log.info("GET /api/expenses/summary/monthly - Fetching monthly summary");
        MonthlySummaryResponse summary = expenseService.getMonthlySummary();
        return ResponseEntity.ok(summary);
    }
}
