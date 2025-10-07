package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.model.ExpenseCategory;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @InjectMocks
    private ExpenseService expenseService;
    
    private Expense testExpense;
    private ExpenseRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setCategory(ExpenseCategory.GROCERIES);
        testExpense.setDate(LocalDate.now());
        testExpense.setDescription("Test expense");
        
        testRequest = new ExpenseRequest();
        testRequest.setAmount(new BigDecimal("50.00"));
        testRequest.setCategory(ExpenseCategory.GROCERIES);
        testRequest.setDate(LocalDate.now());
        testRequest.setDescription("Test expense");
    }
    
    @Test
    void createExpense_ShouldReturnCreatedExpense() {
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        
        ExpenseResponse response = expenseService.createExpense(testRequest);
        
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(testRequest.getAmount());
        assertThat(response.getCategory()).isEqualTo(testRequest.getCategory());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }
    
    @Test
    void getExpenseById_WhenExists_ShouldReturnExpense() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        
        ExpenseResponse response = expenseService.getExpenseById(1L);
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(expenseRepository, times(1)).findById(1L);
    }
    
    @Test
    void getExpenseById_WhenNotExists_ShouldThrowException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Expense not found with id: 999");
    }
    
    @Test
    void updateExpense_WhenExists_ShouldReturnUpdatedExpense() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        
        ExpenseResponse response = expenseService.updateExpense(1L, testRequest);
        
        assertThat(response).isNotNull();
        verify(expenseRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }
    
    @Test
    void deleteExpense_WhenExists_ShouldDeleteExpense() {
        when(expenseRepository.existsById(1L)).thenReturn(true);
        
        expenseService.deleteExpense(1L);
        
        verify(expenseRepository, times(1)).existsById(1L);
        verify(expenseRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void deleteExpense_WhenNotExists_ShouldThrowException() {
        when(expenseRepository.existsById(999L)).thenReturn(false);
        
        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
