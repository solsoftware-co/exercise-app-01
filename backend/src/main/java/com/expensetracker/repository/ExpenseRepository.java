package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.ExpenseCategory;
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
    
    List<Expense> findByCategoryOrderByDateDesc(ExpenseCategory category);
    
    List<Expense> findByCategoryInOrderByDateDesc(List<ExpenseCategory> categories);
    
    List<Expense> findByCategoryAndDateBetweenOrderByDateDesc(
        ExpenseCategory category, LocalDate startDate, LocalDate endDate);
    
    List<Expense> findByCategoryInAndDateBetweenOrderByDateDesc(
        List<ExpenseCategory> categories, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT e.category as category, SUM(e.amount) as total " +
           "FROM Expense e " +
           "GROUP BY e.category " +
           "ORDER BY total DESC")
    List<CategorySummary> findTotalByCategory();
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.date BETWEEN :startDate AND :endDate")
    BigDecimal findTotalAmountBetweenDates(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    interface CategorySummary {
        ExpenseCategory getCategory();
        BigDecimal getTotal();
    }
}
