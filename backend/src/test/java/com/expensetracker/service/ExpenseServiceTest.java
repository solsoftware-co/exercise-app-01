package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.CategoryRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Tests")
class ExpenseServiceTest {
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ExpenseService expenseService;
    
    private Expense testExpense;
    private ExpenseRequest testRequest;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Groceries");
        testCategory.setDescription("Food and household items");
        testCategory.setIsDefault(true);
        
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setCategory(testCategory);
        testExpense.setDate(LocalDate.now());
        testExpense.setDescription("Test expense");
        
        testRequest = new ExpenseRequest();
        testRequest.setAmount(new BigDecimal("50.00"));
        testRequest.setCategoryId(1L);
        testRequest.setDate(LocalDate.now());
        testRequest.setDescription("Test expense");
    }
    
    @Test
    @DisplayName("Should create expense successfully")
    void createExpense_ShouldReturnCreatedExpense() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        
        // Act
        ExpenseResponse response = expenseService.createExpense(testRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(testRequest.getAmount());
        assertThat(response.getCategory()).isEqualTo("Groceries");
        verify(categoryRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }
    
    @Test
    @DisplayName("Should throw exception when category not found during creation")
    void createExpense_WhenCategoryNotFound_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> expenseService.createExpense(testRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category not found");
    }
    
    @Test
    @DisplayName("Should get expense by id when exists")
    void getExpenseById_WhenExists_ShouldReturnExpense() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        
        // Act
        ExpenseResponse response = expenseService.getExpenseById(1L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualTo(testExpense.getAmount());
        verify(expenseRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when expense not found")
    void getExpenseById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Expense not found with id: 999");
    }
    
    @Test
    @DisplayName("Should get all expenses")
    void getAllExpenses_ShouldReturnAllExpenses() {
        // Arrange
        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setAmount(new BigDecimal("75.00"));
        expense2.setCategory(testCategory);
        expense2.setDate(LocalDate.now());
        expense2.setDescription("Another expense");
        
        when(expenseRepository.findAll()).thenReturn(Arrays.asList(testExpense, expense2));
        
        // Act
        List<ExpenseResponse> responses = expenseService.getAllExpenses();
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        verify(expenseRepository, times(1)).findAll();
    }
    
    @Test
    @DisplayName("Should update expense when exists")
    void updateExpense_WhenExists_ShouldReturnUpdatedExpense() {
        // Arrange
        ExpenseRequest updateRequest = new ExpenseRequest();
        updateRequest.setAmount(new BigDecimal("100.00"));
        updateRequest.setCategoryId(1L);
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Updated expense");
        
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        
        // Act
        ExpenseResponse response = expenseService.updateExpense(1L, updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        verify(expenseRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent expense")
    void updateExpense_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(999L, testRequest))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    
    @Test
    @DisplayName("Should delete expense when exists")
    void deleteExpense_WhenExists_ShouldDeleteExpense() {
        // Arrange
        when(expenseRepository.existsById(1L)).thenReturn(true);
        
        // Act
        expenseService.deleteExpense(1L);
        
        // Assert
        verify(expenseRepository, times(1)).existsById(1L);
        verify(expenseRepository, times(1)).deleteById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent expense")
    void deleteExpense_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(expenseRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Expense not found");
    }
    
    @Test
    @DisplayName("Should get expenses filtered by date range only")
    void getFilteredExpenses_WithDateRangeOnly_ShouldReturnFilteredExpenses() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(expenseRepository.findByDateBetweenOrderByDateDesc(startDate, endDate))
            .thenReturn(Arrays.asList(testExpense));
        
        // Act
        List<ExpenseResponse> responses = expenseService.getFilteredExpenses(null, startDate, endDate);
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        verify(expenseRepository, times(1)).findByDateBetweenOrderByDateDesc(startDate, endDate);
    }
    
    @Test
    @DisplayName("Should get expenses filtered by category names only")
    void getFilteredExpenses_WithCategoriesOnly_ShouldReturnFilteredExpenses() {
        // Arrange
        List<String> categories = Arrays.asList("Groceries");
        when(expenseRepository.findByCategoryNamesOrderByDateDesc(categories))
            .thenReturn(Arrays.asList(testExpense));
        
        // Act
        List<ExpenseResponse> responses = expenseService.getFilteredExpenses(categories, null, null);
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("Groceries");
        verify(expenseRepository, times(1)).findByCategoryNamesOrderByDateDesc(categories);
    }
    
    @Test
    @DisplayName("Should get expenses filtered by both categories and date range")
    void getFilteredExpenses_WithCategoriesAndDateRange_ShouldReturnFilteredExpenses() {
        // Arrange
        List<String> categories = Arrays.asList("Groceries");
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(expenseRepository.findByCategoryNamesAndDateBetweenOrderByDateDesc(categories, startDate, endDate))
            .thenReturn(Arrays.asList(testExpense));
        
        // Act
        List<ExpenseResponse> responses = expenseService.getFilteredExpenses(categories, startDate, endDate);
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        verify(expenseRepository, times(1)).findByCategoryNamesAndDateBetweenOrderByDateDesc(categories, startDate, endDate);
    }
    
    @Test
    @DisplayName("Should get all expenses when no filters provided")
    void getFilteredExpenses_WithNoFilters_ShouldReturnAllExpenses() {
        // Arrange
        when(expenseRepository.findAll()).thenReturn(Arrays.asList(testExpense));
        
        // Act
        List<ExpenseResponse> responses = expenseService.getFilteredExpenses(null, null, null);
        
        // Assert
        assertThat(responses).hasSize(1);
        verify(expenseRepository, times(1)).findAll();
    }
}
