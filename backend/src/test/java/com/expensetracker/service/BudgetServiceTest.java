package com.expensetracker.service;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.dto.BudgetStatusResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Budget;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService Tests")
class BudgetServiceTest {
    
    @Mock
    private BudgetRepository budgetRepository;
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @InjectMocks
    private BudgetService budgetService;
    
    private Budget testBudget;
    private BudgetRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setMonthlyLimit(new BigDecimal("2000.00"));
        
        testRequest = new BudgetRequest();
        testRequest.setMonthlyLimit(new BigDecimal("2000.00"));
    }
    
    @Test
    @DisplayName("Should get budget when exists")
    void getBudget_WhenExists_ShouldReturnBudget() {
        // Arrange
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        
        // Act
        BudgetResponse response = budgetService.getBudget();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyLimit()).isEqualTo(new BigDecimal("2000.00"));
        verify(budgetRepository, times(1)).findFirstByOrderByUpdatedAtDesc();
    }
    
    @Test
    @DisplayName("Should throw exception when no budget set")
    void getBudget_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> budgetService.getBudget())
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No budget has been set");
    }
    
    @Test
    @DisplayName("Should set new budget when none exists")
    void setBudget_WhenNoBudgetExists_ShouldCreateNewBudget() {
        // Arrange
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        
        // Act
        BudgetResponse response = budgetService.setBudget(testRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyLimit()).isEqualTo(testRequest.getMonthlyLimit());
        verify(budgetRepository, times(1)).findFirstByOrderByUpdatedAtDesc();
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }
    
    @Test
    @DisplayName("Should update existing budget")
    void setBudget_WhenBudgetExists_ShouldUpdateBudget() {
        // Arrange
        BudgetRequest updateRequest = new BudgetRequest();
        updateRequest.setMonthlyLimit(new BigDecimal("3000.00"));
        
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        
        // Act
        BudgetResponse response = budgetService.setBudget(updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        verify(budgetRepository, times(1)).findFirstByOrderByUpdatedAtDesc();
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }
    
    @Test
    @DisplayName("Should calculate budget status as HEALTHY when under 80%")
    void getBudgetStatus_WhenUnder80Percent_ShouldReturnHealthy() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        when(expenseRepository.findTotalAmountBetweenDates(startDate, endDate))
            .thenReturn(new BigDecimal("1000.00")); // 50% of budget
        
        // Act
        BudgetStatusResponse response = budgetService.getBudgetStatus();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyLimit()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(response.getTotalSpent()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(response.getRemaining()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(response.getPercentageUsed()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getStatus()).isEqualTo(BudgetStatusResponse.BudgetStatus.HEALTHY);
    }
    
    @Test
    @DisplayName("Should calculate budget status as WARNING when between 80-100%")
    void getBudgetStatus_WhenBetween80And100Percent_ShouldReturnWarning() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        when(expenseRepository.findTotalAmountBetweenDates(startDate, endDate))
            .thenReturn(new BigDecimal("1800.00")); // 90% of budget
        
        // Act
        BudgetStatusResponse response = budgetService.getBudgetStatus();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BudgetStatusResponse.BudgetStatus.WARNING);
        assertThat(response.getPercentageUsed()).isEqualByComparingTo(new BigDecimal("90.00"));
    }
    
    @Test
    @DisplayName("Should calculate budget status as OVER_BUDGET when over 100%")
    void getBudgetStatus_WhenOver100Percent_ShouldReturnOverBudget() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        when(expenseRepository.findTotalAmountBetweenDates(startDate, endDate))
            .thenReturn(new BigDecimal("2500.00")); // 125% of budget
        
        // Act
        BudgetStatusResponse response = budgetService.getBudgetStatus();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BudgetStatusResponse.BudgetStatus.OVER_BUDGET);
        assertThat(response.getRemaining()).isEqualByComparingTo(new BigDecimal("-500.00"));
        assertThat(response.getPercentageUsed()).isEqualByComparingTo(new BigDecimal("125.00"));
    }
    
    @Test
    @DisplayName("Should handle null total spent as zero")
    void getBudgetStatus_WhenNoExpenses_ShouldReturnZeroSpent() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.of(testBudget));
        when(expenseRepository.findTotalAmountBetweenDates(startDate, endDate))
            .thenReturn(null);
        
        // Act
        BudgetStatusResponse response = budgetService.getBudgetStatus();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getPercentageUsed()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo(BudgetStatusResponse.BudgetStatus.HEALTHY);
    }
    
    @Test
    @DisplayName("Should throw exception when getting status with no budget set")
    void getBudgetStatus_WhenNoBudget_ShouldThrowException() {
        // Arrange
        when(budgetRepository.findFirstByOrderByUpdatedAtDesc())
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> budgetService.getBudgetStatus())
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No budget has been set");
    }
}
