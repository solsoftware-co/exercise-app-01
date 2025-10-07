package com.expensetracker.controller;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.dto.BudgetStatusResponse;
import com.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BudgetController {
    
    private final BudgetService budgetService;
    
    @GetMapping
    public ResponseEntity<BudgetResponse> getBudget() {
        log.info("GET /api/budget - Fetching budget");
        BudgetResponse budget = budgetService.getBudget();
        return ResponseEntity.ok(budget);
    }
    
    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(@Valid @RequestBody BudgetRequest request) {
        log.info("POST /api/budget - Setting budget");
        BudgetResponse budget = budgetService.setBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }
    
    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getBudgetStatus() {
        log.info("GET /api/budget/status - Fetching budget status");
        BudgetStatusResponse status = budgetService.getBudgetStatus();
        return ResponseEntity.ok(status);
    }
}
