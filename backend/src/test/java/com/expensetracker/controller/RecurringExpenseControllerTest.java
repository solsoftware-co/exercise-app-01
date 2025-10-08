package com.expensetracker.controller;

import com.expensetracker.dto.RecurringExpenseRequest;
import com.expensetracker.dto.RecurringExpenseResponse;
import com.expensetracker.model.RecurringExpense;
import com.expensetracker.service.RecurringExpenseService;
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

@WebMvcTest(RecurringExpenseController.class)
@DisplayName("RecurringExpenseController Tests")
class RecurringExpenseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RecurringExpenseService recurringExpenseService;
    
    private RecurringExpenseResponse testRecurringExpenseResponse;
    private RecurringExpenseRequest testRecurringExpenseRequest;
    
    @BeforeEach
    void setUp() {
        testRecurringExpenseResponse = new RecurringExpenseResponse();
        testRecurringExpenseResponse.setId(1L);
        testRecurringExpenseResponse.setAmount(new BigDecimal("100.00"));
        testRecurringExpenseResponse.setCategory("Utilities");
        testRecurringExpenseResponse.setDescription("Monthly rent");
        testRecurringExpenseResponse.setFrequency(RecurringExpense.RecurrenceFrequency.MONTHLY);
        testRecurringExpenseResponse.setStartDate(LocalDate.now());
        testRecurringExpenseResponse.setNextOccurrence(LocalDate.now());
        testRecurringExpenseResponse.setActive(true);
        
        testRecurringExpenseRequest = new RecurringExpenseRequest();
        testRecurringExpenseRequest.setAmount(new BigDecimal("100.00"));
        testRecurringExpenseRequest.setCategoryId(1L);
        testRecurringExpenseRequest.setDescription("Monthly rent");
        testRecurringExpenseRequest.setFrequency(RecurringExpense.RecurrenceFrequency.MONTHLY);
        testRecurringExpenseRequest.setStartDate(LocalDate.now());
    }
    
    @Test
    @DisplayName("GET /api/recurring-expenses - Should return all recurring expenses")
    void getAllRecurringExpenses_ShouldReturnAll() throws Exception {
        when(recurringExpenseService.getAllRecurringExpenses())
                .thenReturn(Arrays.asList(testRecurringExpenseResponse));
        
        mockMvc.perform(get("/api/recurring-expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.00));
        
        verify(recurringExpenseService, times(1)).getAllRecurringExpenses();
    }
    
    @Test
    @DisplayName("GET /api/recurring-expenses/active - Should return active recurring expenses")
    void getActiveRecurringExpenses_ShouldReturnActive() throws Exception {
        when(recurringExpenseService.getActiveRecurringExpenses())
                .thenReturn(Arrays.asList(testRecurringExpenseResponse));
        
        mockMvc.perform(get("/api/recurring-expenses/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
        
        verify(recurringExpenseService, times(1)).getActiveRecurringExpenses();
    }
    
    @Test
    @DisplayName("GET /api/recurring-expenses/{id} - Should return recurring expense by id")
    void getRecurringExpenseById_ShouldReturnExpense() throws Exception {
        when(recurringExpenseService.getRecurringExpenseById(1L))
                .thenReturn(testRecurringExpenseResponse);
        
        mockMvc.perform(get("/api/recurring-expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(recurringExpenseService, times(1)).getRecurringExpenseById(1L);
    }
    
    @Test
    @DisplayName("POST /api/recurring-expenses - Should create recurring expense")
    void createRecurringExpense_ShouldCreateExpense() throws Exception {
        when(recurringExpenseService.createRecurringExpense(any(RecurringExpenseRequest.class)))
                .thenReturn(testRecurringExpenseResponse);
        
        mockMvc.perform(post("/api/recurring-expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRecurringExpenseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(recurringExpenseService, times(1))
                .createRecurringExpense(any(RecurringExpenseRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/recurring-expenses/{id} - Should update recurring expense")
    void updateRecurringExpense_ShouldUpdateExpense() throws Exception {
        when(recurringExpenseService.updateRecurringExpense(eq(1L), any(RecurringExpenseRequest.class)))
                .thenReturn(testRecurringExpenseResponse);
        
        mockMvc.perform(put("/api/recurring-expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRecurringExpenseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(recurringExpenseService, times(1))
                .updateRecurringExpense(eq(1L), any(RecurringExpenseRequest.class));
    }
    
    @Test
    @DisplayName("DELETE /api/recurring-expenses/{id} - Should delete recurring expense")
    void deleteRecurringExpense_ShouldDeleteExpense() throws Exception {
        doNothing().when(recurringExpenseService).deleteRecurringExpense(1L);
        
        mockMvc.perform(delete("/api/recurring-expenses/1"))
                .andExpect(status().isNoContent());
        
        verify(recurringExpenseService, times(1)).deleteRecurringExpense(1L);
    }
    
    @Test
    @DisplayName("PATCH /api/recurring-expenses/{id}/toggle - Should toggle recurring expense")
    void toggleRecurringExpense_ShouldToggle() throws Exception {
        doNothing().when(recurringExpenseService).toggleRecurringExpense(1L, true);
        
        mockMvc.perform(patch("/api/recurring-expenses/1/toggle")
                        .param("active", "true"))
                .andExpect(status().isOk());
        
        verify(recurringExpenseService, times(1)).toggleRecurringExpense(1L, true);
    }
    
    @Test
    @DisplayName("POST /api/recurring-expenses/process - Should process recurring expenses")
    void processRecurringExpenses_ShouldProcess() throws Exception {
        doNothing().when(recurringExpenseService).processRecurringExpenses();
        
        mockMvc.perform(post("/api/recurring-expenses/process"))
                .andExpect(status().isOk());
        
        verify(recurringExpenseService, times(1)).processRecurringExpenses();
    }
}
