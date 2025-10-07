package com.expensetracker.config;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {
    
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    
    @Bean
    @Profile("dev")
    public CommandLineRunner seedData() {
        return args -> {
            // Seed default categories first
            if (categoryRepository.count() == 0) {
                log.info("Seeding default categories...");
                List<Category> defaultCategories = Arrays.asList(
                    createCategory("Groceries", "Food and household items", true),
                    createCategory("Transportation", "Gas, public transit, parking", true),
                    createCategory("Entertainment", "Movies, games, subscriptions", true),
                    createCategory("Utilities", "Electricity, water, internet", true),
                    createCategory("Other", "Miscellaneous expenses", true)
                );
                categoryRepository.saveAll(defaultCategories);
                log.info("Successfully seeded {} default categories", defaultCategories.size());
            }
            
            // Only seed expenses if database is empty
            if (expenseRepository.count() > 0) {
                log.info("Database already contains expense data. Skipping expense seeding.");
                return;
            }
            
            log.info("Seeding database with sample expense data...");
            
            // Fetch categories for use in expenses
            Category groceries = categoryRepository.findByName("Groceries").orElseThrow();
            Category transportation = categoryRepository.findByName("Transportation").orElseThrow();
            Category entertainment = categoryRepository.findByName("Entertainment").orElseThrow();
            Category utilities = categoryRepository.findByName("Utilities").orElseThrow();
            Category other = categoryRepository.findByName("Other").orElseThrow();
            
            List<Expense> sampleExpenses = Arrays.asList(
                // Current month expenses
                createExpense(new BigDecimal("85.50"), groceries, 
                    LocalDate.now().minusDays(2), "Weekly grocery shopping at Whole Foods"),
                createExpense(new BigDecimal("45.00"), transportation, 
                    LocalDate.now().minusDays(3), "Gas fill-up"),
                createExpense(new BigDecimal("120.00"), utilities, 
                    LocalDate.now().minusDays(5), "Monthly electricity bill"),
                createExpense(new BigDecimal("15.99"), entertainment, 
                    LocalDate.now().minusDays(1), "Netflix subscription"),
                createExpense(new BigDecimal("67.30"), groceries, 
                    LocalDate.now().minusDays(7), "Groceries at Trader Joe's"),
                createExpense(new BigDecimal("25.00"), transportation, 
                    LocalDate.now().minusDays(4), "Uber ride to downtown"),
                createExpense(new BigDecimal("89.99"), entertainment, 
                    LocalDate.now().minusDays(6), "Concert tickets"),
                createExpense(new BigDecimal("12.50"), other, 
                    LocalDate.now().minusDays(1), "Coffee and pastry"),
                createExpense(new BigDecimal("150.00"), utilities, 
                    LocalDate.now().minusDays(8), "Internet and phone bill"),
                createExpense(new BigDecimal("42.75"), groceries, 
                    LocalDate.now().minusDays(10), "Fresh produce at farmer's market"),
                
                // Last week
                createExpense(new BigDecimal("35.00"), transportation, 
                    LocalDate.now().minusDays(9), "Parking fee"),
                createExpense(new BigDecimal("78.20"), groceries, 
                    LocalDate.now().minusDays(12), "Grocery shopping"),
                createExpense(new BigDecimal("19.99"), entertainment, 
                    LocalDate.now().minusDays(11), "Movie tickets"),
                createExpense(new BigDecimal("55.00"), other, 
                    LocalDate.now().minusDays(13), "Haircut"),
                createExpense(new BigDecimal("95.00"), utilities, 
                    LocalDate.now().minusDays(14), "Water bill"),
                
                // Earlier this month
                createExpense(new BigDecimal("110.50"), groceries, 
                    LocalDate.now().minusDays(15), "Monthly grocery haul"),
                createExpense(new BigDecimal("30.00"), transportation, 
                    LocalDate.now().minusDays(16), "Gas"),
                createExpense(new BigDecimal("45.99"), entertainment, 
                    LocalDate.now().minusDays(17), "Video game purchase"),
                createExpense(new BigDecimal("22.50"), other, 
                    LocalDate.now().minusDays(18), "Lunch with friends"),
                createExpense(new BigDecimal("8.99"), entertainment, 
                    LocalDate.now().minusDays(19), "Spotify subscription"),
                
                // Last month
                createExpense(new BigDecimal("125.00"), utilities, 
                    LocalDate.now().minusMonths(1).minusDays(5), "Previous month electricity"),
                createExpense(new BigDecimal("92.30"), groceries, 
                    LocalDate.now().minusMonths(1).minusDays(10), "Grocery shopping"),
                createExpense(new BigDecimal("40.00"), transportation, 
                    LocalDate.now().minusMonths(1).minusDays(8), "Gas"),
                createExpense(new BigDecimal("65.00"), entertainment, 
                    LocalDate.now().minusMonths(1).minusDays(12), "Restaurant dinner"),
                createExpense(new BigDecimal("18.50"), other, 
                    LocalDate.now().minusMonths(1).minusDays(15), "Coffee shop")
            );
            
            expenseRepository.saveAll(sampleExpenses);
            log.info("Successfully seeded {} expense records", sampleExpenses.size());
        };
    }
    
    private Category createCategory(String name, String description, boolean isDefault) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIsDefault(isDefault);
        return category;
    }
    
    private Expense createExpense(BigDecimal amount, Category category, 
                                  LocalDate date, String description) {
        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setDescription(description);
        return expense;
    }
}
