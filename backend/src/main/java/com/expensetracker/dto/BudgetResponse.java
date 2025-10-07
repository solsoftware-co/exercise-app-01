package com.expensetracker.dto;

import com.expensetracker.model.Budget;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private BigDecimal monthlyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BudgetResponse fromEntity(Budget budget) {
        return new BudgetResponse(
            budget.getId(),
            budget.getMonthlyLimit(),
            budget.getCreatedAt(),
            budget.getUpdatedAt()
        );
    }
}
