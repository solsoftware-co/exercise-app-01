package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusResponse {
    private BigDecimal monthlyLimit;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private BigDecimal percentageUsed;
    private BudgetStatus status;
    
    public enum BudgetStatus {
        HEALTHY,      // < 80%
        WARNING,      // >= 80% and < 100%
        OVER_BUDGET   // >= 100%
    }
}
