package com.expensetracker.dto;

import com.expensetracker.model.RecurringExpense;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseResponse {
    private Long id;
    private BigDecimal amount;
    private String category;
    private String description;
    private RecurringExpense.RecurrenceFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextOccurrence;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RecurringExpenseResponse fromEntity(RecurringExpense recurringExpense) {
        return new RecurringExpenseResponse(
            recurringExpense.getId(),
            recurringExpense.getAmount(),
            recurringExpense.getCategory().getName(),
            recurringExpense.getDescription(),
            recurringExpense.getFrequency(),
            recurringExpense.getStartDate(),
            recurringExpense.getEndDate(),
            recurringExpense.getNextOccurrence(),
            recurringExpense.getActive(),
            recurringExpense.getCreatedAt(),
            recurringExpense.getUpdatedAt()
        );
    }
}
