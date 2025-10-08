package com.expensetracker.service;

import com.expensetracker.dto.RecurringExpenseRequest;
import com.expensetracker.dto.RecurringExpenseResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.RecurringExpense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.RecurringExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("RecurringExpenseService Tests")
class RecurringExpenseServiceTest {
    
    @Mock
    private RecurringExpenseRepository recurringExpenseRepository;
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private RecurringExpenseService recurringExpenseService;
    
    private RecurringExpense testRecurringExpense;
    private RecurringExpenseRequest testRequest;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Utilities");
        testCategory.setDescription("Monthly utilities");
        testCategory.setIsDefault(true);
        
        testRecurringExpense = new RecurringExpense();
        testRecurringExpense.setId(1L);
        testRecurringExpense.setAmount(new BigDecimal("100.00"));
        testRecurringExpense.setCategory(testCategory);
        testRecurringExpense.setDescription("Monthly rent");
        testRecurringExpense.setFrequency(RecurringExpense.RecurrenceFrequency.MONTHLY);
        testRecurringExpense.setStartDate(LocalDate.now());
        testRecurringExpense.setNextOccurrence(LocalDate.now());
        testRecurringExpense.setActive(true);
        
        testRequest = new RecurringExpenseRequest();
        testRequest.setAmount(new BigDecimal("100.00"));
        testRequest.setCategoryId(1L);
        testRequest.setDescription("Monthly rent");
        testRequest.setFrequency(RecurringExpense.RecurrenceFrequency.MONTHLY);
        testRequest.setStartDate(LocalDate.now());
    }
    
    @Test
    @DisplayName("Should get all recurring expenses")
    void getAllRecurringExpenses_ShouldReturnAllRecurringExpenses() {
        // Arrange
        when(recurringExpenseRepository.findAll())
            .thenReturn(Arrays.asList(testRecurringExpense));
        
        // Act
        List<RecurringExpenseResponse> responses = recurringExpenseService.getAllRecurringExpenses();
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        verify(recurringExpenseRepository, times(1)).findAll();
    }
    
    @Test
    @DisplayName("Should get only active recurring expenses")
    void getActiveRecurringExpenses_ShouldReturnOnlyActiveExpenses() {
        // Arrange
        when(recurringExpenseRepository.findByActiveTrue())
            .thenReturn(Arrays.asList(testRecurringExpense));
        
        // Act
        List<RecurringExpenseResponse> responses = recurringExpenseService.getActiveRecurringExpenses();
        
        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getActive()).isTrue();
        verify(recurringExpenseRepository, times(1)).findByActiveTrue();
    }
    
    @Test
    @DisplayName("Should get recurring expense by id when exists")
    void getRecurringExpenseById_WhenExists_ShouldReturnRecurringExpense() {
        // Arrange
        when(recurringExpenseRepository.findById(1L))
            .thenReturn(Optional.of(testRecurringExpense));
        
        // Act
        RecurringExpenseResponse response = recurringExpenseService.getRecurringExpenseById(1L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("100.00"));
        verify(recurringExpenseRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when recurring expense not found")
    void getRecurringExpenseById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(recurringExpenseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> recurringExpenseService.getRecurringExpenseById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Recurring expense not found with id: 999");
    }
    
    @Test
    @DisplayName("Should create recurring expense successfully")
    void createRecurringExpense_ShouldReturnCreatedRecurringExpense() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        RecurringExpenseResponse response = recurringExpenseService.createRecurringExpense(testRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(testRequest.getAmount());
        assertThat(response.getActive()).isTrue();
        verify(categoryRepository, times(1)).findById(1L);
        verify(recurringExpenseRepository, times(1)).save(any(RecurringExpense.class));
    }
    
    @Test
    @DisplayName("Should throw exception when category not found during creation")
    void createRecurringExpense_WhenCategoryNotFound_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> recurringExpenseService.createRecurringExpense(testRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category not found");
    }
    
    @Test
    @DisplayName("Should update recurring expense when exists")
    void updateRecurringExpense_WhenExists_ShouldReturnUpdatedRecurringExpense() {
        // Arrange
        RecurringExpenseRequest updateRequest = new RecurringExpenseRequest();
        updateRequest.setAmount(new BigDecimal("150.00"));
        updateRequest.setCategoryId(1L);
        updateRequest.setDescription("Updated rent");
        updateRequest.setFrequency(RecurringExpense.RecurrenceFrequency.MONTHLY);
        updateRequest.setStartDate(LocalDate.now());
        
        when(recurringExpenseRepository.findById(1L))
            .thenReturn(Optional.of(testRecurringExpense));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        RecurringExpenseResponse response = recurringExpenseService.updateRecurringExpense(1L, updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        verify(recurringExpenseRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(recurringExpenseRepository, times(1)).save(any(RecurringExpense.class));
    }
    
    @Test
    @DisplayName("Should delete recurring expense when exists")
    void deleteRecurringExpense_WhenExists_ShouldDeleteRecurringExpense() {
        // Arrange
        when(recurringExpenseRepository.existsById(1L)).thenReturn(true);
        
        // Act
        recurringExpenseService.deleteRecurringExpense(1L);
        
        // Assert
        verify(recurringExpenseRepository, times(1)).existsById(1L);
        verify(recurringExpenseRepository, times(1)).deleteById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent recurring expense")
    void deleteRecurringExpense_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(recurringExpenseRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> recurringExpenseService.deleteRecurringExpense(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Recurring expense not found");
    }
    
    @Test
    @DisplayName("Should toggle recurring expense to active")
    void toggleRecurringExpense_ShouldUpdateActiveStatus() {
        // Arrange
        testRecurringExpense.setActive(false);
        when(recurringExpenseRepository.findById(1L))
            .thenReturn(Optional.of(testRecurringExpense));
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        recurringExpenseService.toggleRecurringExpense(1L, true);
        
        // Assert
        verify(recurringExpenseRepository, times(1)).findById(1L);
        verify(recurringExpenseRepository, times(1)).save(testRecurringExpense);
    }
    
    @Test
    @DisplayName("Should process due recurring expenses and create expenses")
    void processRecurringExpenses_WhenDue_ShouldCreateExpenses() {
        // Arrange
        LocalDate today = LocalDate.now();
        testRecurringExpense.setNextOccurrence(today);
        
        when(recurringExpenseRepository.findByActiveTrueAndNextOccurrenceLessThanEqual(today))
            .thenReturn(Arrays.asList(testRecurringExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        recurringExpenseService.processRecurringExpenses();
        
        // Assert
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository, times(1)).save(expenseCaptor.capture());
        
        Expense createdExpense = expenseCaptor.getValue();
        assertThat(createdExpense.getAmount()).isEqualTo(testRecurringExpense.getAmount());
        assertThat(createdExpense.getCategory()).isEqualTo(testRecurringExpense.getCategory());
        assertThat(createdExpense.getDescription()).contains("(Recurring)");
        
        verify(recurringExpenseRepository, times(1)).save(testRecurringExpense);
    }
    
    @Test
    @DisplayName("Should deactivate recurring expense when end date reached")
    void processRecurringExpenses_WhenEndDateReached_ShouldDeactivate() {
        // Arrange
        LocalDate today = LocalDate.now();
        testRecurringExpense.setNextOccurrence(today);
        testRecurringExpense.setEndDate(today.minusDays(1)); // End date in the past
        
        when(recurringExpenseRepository.findByActiveTrueAndNextOccurrenceLessThanEqual(today))
            .thenReturn(Arrays.asList(testRecurringExpense));
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        recurringExpenseService.processRecurringExpenses();
        
        // Assert
        verify(recurringExpenseRepository, times(1)).save(testRecurringExpense);
        verify(expenseRepository, never()).save(any(Expense.class));
    }
    
    @Test
    @DisplayName("Should calculate next occurrence for different frequencies")
    void processRecurringExpenses_ShouldCalculateCorrectNextOccurrence() {
        // Arrange
        LocalDate today = LocalDate.now();
        testRecurringExpense.setNextOccurrence(today);
        testRecurringExpense.setFrequency(RecurringExpense.RecurrenceFrequency.WEEKLY);
        
        when(recurringExpenseRepository.findByActiveTrueAndNextOccurrenceLessThanEqual(today))
            .thenReturn(Arrays.asList(testRecurringExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());
        when(recurringExpenseRepository.save(any(RecurringExpense.class)))
            .thenReturn(testRecurringExpense);
        
        // Act
        recurringExpenseService.processRecurringExpenses();
        
        // Assert
        ArgumentCaptor<RecurringExpense> captor = ArgumentCaptor.forClass(RecurringExpense.class);
        verify(recurringExpenseRepository, times(1)).save(captor.capture());
        
        RecurringExpense saved = captor.getValue();
        assertThat(saved.getNextOccurrence()).isEqualTo(today.plusWeeks(1));
    }
}
