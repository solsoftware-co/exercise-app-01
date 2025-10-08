package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT e FROM Expense e WHERE e.category.name IN :categoryNames ORDER BY e.date DESC")
    List<Expense> findByCategoryNamesOrderByDateDesc(@Param("categoryNames") List<String> categoryNames);
    
    @Query("SELECT e FROM Expense e WHERE e.category.name IN :categoryNames AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
    List<Expense> findByCategoryNamesAndDateBetweenOrderByDateDesc(
        @Param("categoryNames") List<String> categoryNames, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e.category.name as category, SUM(e.amount) as total " +
           "FROM Expense e " +
           "GROUP BY e.category.name " +
           "ORDER BY total DESC")
    List<CategorySummary> findTotalByCategory();
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    BigDecimal findTotalAmountBetweenDates(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    interface CategorySummary {
        String getCategory();
        BigDecimal getTotal();
    }
}
