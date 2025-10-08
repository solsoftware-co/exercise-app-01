package com.expensetracker.controller;

import com.expensetracker.dto.*;
import com.expensetracker.service.ExpenseService;
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
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@DisplayName("ExpenseController Tests")
class ExpenseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ExpenseService expenseService;
    
    private ExpenseResponse testExpenseResponse;
    private ExpenseRequest testExpenseRequest;
    
    @BeforeEach
    void setUp() {
        testExpenseResponse = new ExpenseResponse();
        testExpenseResponse.setId(1L);
        testExpenseResponse.setAmount(new BigDecimal("50.00"));
        testExpenseResponse.setCategory("Groceries");
        testExpenseResponse.setDate(LocalDate.now());
        testExpenseResponse.setDescription("Test expense");
        
        testExpenseRequest = new ExpenseRequest();
        testExpenseRequest.setAmount(new BigDecimal("50.00"));
        testExpenseRequest.setCategoryId(1L);
        testExpenseRequest.setDate(LocalDate.now());
        testExpenseRequest.setDescription("Test expense");
    }
    
    @Test
    @DisplayName("GET /api/expenses - Should return all expenses")
    void getAllExpenses_ShouldReturnAllExpenses() throws Exception {
        when(expenseService.getAllExpenses()).thenReturn(Arrays.asList(testExpenseResponse));
        
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].category").value("Groceries"));
        
        verify(expenseService, times(1)).getAllExpenses();
    }
    
    @Test
    @DisplayName("GET /api/expenses/filter - Should return filtered expenses")
    void getFilteredExpenses_ShouldReturnFilteredExpenses() throws Exception {
        when(expenseService.getFilteredExpenses(any(), any(), any()))
                .thenReturn(Arrays.asList(testExpenseResponse));
        
        mockMvc.perform(get("/api/expenses/filter")
                        .param("categories", "Groceries")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        
        verify(expenseService, times(1)).getFilteredExpenses(any(), any(), any());
    }
    
    @Test
    @DisplayName("GET /api/expenses/{id} - Should return expense by id")
    void getExpenseById_ShouldReturnExpense() throws Exception {
        when(expenseService.getExpenseById(1L)).thenReturn(testExpenseResponse);
        
        mockMvc.perform(get("/api/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50.00));
        
        verify(expenseService, times(1)).getExpenseById(1L);
    }
    
    @Test
    @DisplayName("POST /api/expenses - Should create expense")
    void createExpense_ShouldCreateExpense() throws Exception {
        when(expenseService.createExpense(any(ExpenseRequest.class)))
                .thenReturn(testExpenseResponse);
        
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(expenseService, times(1)).createExpense(any(ExpenseRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/expenses/{id} - Should update expense")
    void updateExpense_ShouldUpdateExpense() throws Exception {
        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class)))
                .thenReturn(testExpenseResponse);
        
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testExpenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(expenseService, times(1)).updateExpense(eq(1L), any(ExpenseRequest.class));
    }
    
    @Test
    @DisplayName("DELETE /api/expenses/{id} - Should delete expense")
    void deleteExpense_ShouldDeleteExpense() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);
        
        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());
        
        verify(expenseService, times(1)).deleteExpense(1L);
    }
    
    @Test
    @DisplayName("GET /api/expenses/summary/by-category - Should return category summary")
    void getCategorySummary_ShouldReturnSummary() throws Exception {
        CategorySummaryResponse summary = new CategorySummaryResponse("Groceries", new BigDecimal("100.00"));
        when(expenseService.getCategorySummary()).thenReturn(Arrays.asList(summary));
        
        mockMvc.perform(get("/api/expenses/summary/by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Groceries"))
                .andExpect(jsonPath("$[0].total").value(100.00));
        
        verify(expenseService, times(1)).getCategorySummary();
    }
    
    @Test
    @DisplayName("GET /api/expenses/summary/monthly - Should return monthly summary")
    void getMonthlySummary_ShouldReturnSummary() throws Exception {
        MonthlySummaryResponse summary = new MonthlySummaryResponse(new BigDecimal("500.00"), 10, 2025);
        when(expenseService.getMonthlySummary()).thenReturn(summary);
        
        mockMvc.perform(get("/api/expenses/summary/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(500.00))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2025));
        
        verify(expenseService, times(1)).getMonthlySummary();
    }
}
