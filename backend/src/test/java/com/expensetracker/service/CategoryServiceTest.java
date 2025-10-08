package com.expensetracker.service;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private Category testCategory;
    private Category defaultCategory;
    private CategoryRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Custom Category");
        testCategory.setDescription("A custom category");
        testCategory.setIsDefault(false);
        
        defaultCategory = new Category();
        defaultCategory.setId(2L);
        defaultCategory.setName("Groceries");
        defaultCategory.setDescription("Food and household items");
        defaultCategory.setIsDefault(true);
        
        testRequest = new CategoryRequest();
        testRequest.setName("Custom Category");
        testRequest.setDescription("A custom category");
    }
    
    @Test
    @DisplayName("Should get all categories ordered by name")
    void getAllCategories_ShouldReturnAllCategoriesOrderedByName() {
        // Arrange
        when(categoryRepository.findAllByOrderByNameAsc())
            .thenReturn(Arrays.asList(testCategory, defaultCategory));
        
        // Act
        List<CategoryResponse> responses = categoryService.getAllCategories();
        
        // Assert
        assertThat(responses).hasSize(2);
        verify(categoryRepository, times(1)).findAllByOrderByNameAsc();
    }
    
    @Test
    @DisplayName("Should get category by id when exists")
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        // Act
        CategoryResponse response = categoryService.getCategoryById(1L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Custom Category");
        verify(categoryRepository, times(1)).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when category not found")
    void getCategoryById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category not found with id: 999");
    }
    
    @Test
    @DisplayName("Should create category successfully")
    void createCategory_WhenNameIsUnique_ShouldReturnCreatedCategory() {
        // Arrange
        when(categoryRepository.existsByName("Custom Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        CategoryResponse response = categoryService.createCategory(testRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Custom Category");
        assertThat(response.getIsDefault()).isFalse();
        verify(categoryRepository, times(1)).existsByName("Custom Category");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
    
    @Test
    @DisplayName("Should throw exception when creating category with duplicate name")
    void createCategory_WhenNameExists_ShouldThrowException() {
        // Arrange
        when(categoryRepository.existsByName("Custom Category")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(testRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category with name 'Custom Category' already exists");
        
        verify(categoryRepository, times(1)).existsByName("Custom Category");
        verify(categoryRepository, never()).save(any(Category.class));
    }
    
    @Test
    @DisplayName("Should update custom category successfully")
    void updateCategory_WhenCustomCategory_ShouldReturnUpdatedCategory() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Category");
        updateRequest.setDescription("Updated description");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByName("Updated Category");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating default category")
    void updateCategory_WhenDefaultCategory_ShouldThrowException() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Groceries");
        updateRequest.setDescription("Updated description");
        
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(defaultCategory));
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(2L, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot update default categories");
        
        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating to existing name")
    void updateCategory_WhenNameConflicts_ShouldThrowException() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Groceries");
        updateRequest.setDescription("Updated description");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Groceries")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(1L, updateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category with name 'Groceries' already exists");
        
        verify(categoryRepository, never()).save(any(Category.class));
    }
    
    @Test
    @DisplayName("Should delete custom category successfully")
    void deleteCategory_WhenCustomCategory_ShouldDeleteCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        // Act
        categoryService.deleteCategory(1L);
        
        // Assert
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(testCategory);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting default category")
    void deleteCategory_WhenDefaultCategory_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(defaultCategory));
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(2L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot delete default categories");
        
        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void deleteCategory_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category not found with id: 999");
        
        verify(categoryRepository, never()).delete(any(Category.class));
    }
    
    @Test
    @DisplayName("Should allow updating category with same name")
    void updateCategory_WhenSameName_ShouldNotCheckForDuplicate() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Custom Category"); // Same name
        updateRequest.setDescription("Updated description");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
