package com.expensetracker.controller;

import com.expensetracker.dto.CategoryRequest;
import com.expensetracker.dto.CategoryResponse;
import com.expensetracker.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CategoryService categoryService;
    
    private CategoryResponse testCategoryResponse;
    private CategoryRequest testCategoryRequest;
    
    @BeforeEach
    void setUp() {
        testCategoryResponse = new CategoryResponse();
        testCategoryResponse.setId(1L);
        testCategoryResponse.setName("Groceries");
        testCategoryResponse.setDescription("Food and household items");
        testCategoryResponse.setIsDefault(false);
        
        testCategoryRequest = new CategoryRequest();
        testCategoryRequest.setName("Groceries");
        testCategoryRequest.setDescription("Food and household items");
    }
    
    @Test
    @DisplayName("GET /api/categories - Should return all categories")
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(testCategoryResponse));
        
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Groceries"));
        
        verify(categoryService, times(1)).getAllCategories();
    }
    
    @Test
    @DisplayName("GET /api/categories/{id} - Should return category by id")
    void getCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(testCategoryResponse);
        
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Groceries"));
        
        verify(categoryService, times(1)).getCategoryById(1L);
    }
    
    @Test
    @DisplayName("POST /api/categories - Should create category")
    void createCategory_ShouldCreateCategory() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenReturn(testCategoryResponse);
        
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Groceries"));
        
        verify(categoryService, times(1)).createCategory(any(CategoryRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/categories/{id} - Should update category")
    void updateCategory_ShouldUpdateCategory() throws Exception {
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(testCategoryResponse);
        
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(categoryService, times(1)).updateCategory(eq(1L), any(CategoryRequest.class));
    }
    
    @Test
    @DisplayName("DELETE /api/categories/{id} - Should delete category")
    void deleteCategory_ShouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);
        
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
        
        verify(categoryService, times(1)).deleteCategory(1L);
    }
}
