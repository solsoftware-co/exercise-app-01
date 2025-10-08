package com.expensetracker.controller;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.dto.BudgetStatusResponse;
import com.expensetracker.service.BudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@DisplayName("BudgetController Tests")
class BudgetControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private BudgetService budgetService;
    
    private BudgetResponse testBudgetResponse;
    private BudgetRequest testBudgetRequest;
    
    @BeforeEach
    void setUp() {
        testBudgetResponse = new BudgetResponse();
        testBudgetResponse.setId(1L);
        testBudgetResponse.setMonthlyLimit(new BigDecimal("2000.00"));
        
        testBudgetRequest = new BudgetRequest();
        testBudgetRequest.setMonthlyLimit(new BigDecimal("2000.00"));
    }
    
    @Test
    @DisplayName("GET /api/budget - Should return budget")
    void getBudget_ShouldReturnBudget() throws Exception {
        when(budgetService.getBudget()).thenReturn(testBudgetResponse);
        
        mockMvc.perform(get("/api/budget"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.monthlyLimit").value(2000.00));
        
        verify(budgetService, times(1)).getBudget();
    }
    
    @Test
    @DisplayName("POST /api/budget - Should set budget")
    void setBudget_ShouldSetBudget() throws Exception {
        when(budgetService.setBudget(any(BudgetRequest.class)))
                .thenReturn(testBudgetResponse);
        
        mockMvc.perform(post("/api/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBudgetRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.monthlyLimit").value(2000.00));
        
        verify(budgetService, times(1)).setBudget(any(BudgetRequest.class));
    }
    
    @Test
    @DisplayName("GET /api/budget/status - Should return budget status")
    void getBudgetStatus_ShouldReturnStatus() throws Exception {
        BudgetStatusResponse status = new BudgetStatusResponse(
                new BigDecimal("2000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("50.00"),
                BudgetStatusResponse.BudgetStatus.HEALTHY
        );
        
        when(budgetService.getBudgetStatus()).thenReturn(status);
        
        mockMvc.perform(get("/api/budget/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyLimit").value(2000.00))
                .andExpect(jsonPath("$.totalSpent").value(1000.00))
                .andExpect(jsonPath("$.remaining").value(1000.00))
                .andExpect(jsonPath("$.percentageUsed").value(50.00))
                .andExpect(jsonPath("$.status").value("HEALTHY"));
        
        verify(budgetService, times(1)).getBudgetStatus();
    }
}
