# Category Migration - Custom Expense Categories Feature

## Summary
Migrated from hardcoded `ExpenseCategory` enum to dynamic `Category` entity that users can create, update, and delete.

## Backend Changes ✅ COMPLETED

### New Files Created:
- `Category.java` - Entity for custom categories
- `CategoryRepository.java` - JPA repository
- `CategoryService.java` - Business logic
- `CategoryController.java` - REST API endpoints
- `CategoryRequest.java` & `CategoryResponse.java` - DTOs

### Modified Files:
- `Expense.java` - Changed from enum to ManyToOne relationship with Category
- `RecurringExpense.java` - Changed from enum to ManyToOne relationship with Category
- `ExpenseRequest.java` - Changed `category` field from enum to `categoryId: Long`
- `RecurringExpenseRequest.java` - Changed `category` field from enum to `categoryId: Long`
- `ExpenseResponse.java` - Changed `category` field from enum to `String` (category name)
- `RecurringExpenseResponse.java` - Changed `category` field from enum to `String`
- `CategorySummaryResponse.java` - Changed from enum to String
- `ExpenseRepository.java` - Updated queries to use category names
- `ExpenseService.java` - Added CategoryRepository injection, updated create/update methods
- `RecurringExpenseService.java` - Added CategoryRepository injection, updated create/update methods
- `ExpenseController.java` - Changed filter parameter from `List<ExpenseCategory>` to `List<String>`
- `DataSeeder.java` - Seeds default categories and uses Category entities

### API Endpoints:
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category (cannot delete default categories)

## Frontend Changes

### Completed:
- ✅ Updated `api.ts` - Changed ExpenseCategory enum to Category interface
- ✅ Added `categoryApi` with CRUD operations
- ✅ Created `CategoryManagement.tsx` component

### Still Need to Update:
The following components need to be updated to use dynamic categories:

1. **ExpenseForm.tsx**
   - Remove `ExpenseCategory` import
   - Load categories from API
   - Change category field from enum to dropdown with dynamic categories
   - Use `categoryId` instead of `category` in form data

2. **ExpenseFilter.tsx**
   - Remove `ExpenseCategory` import
   - Load categories from API
   - Use category names for filtering

3. **RecurringExpenses.tsx**
   - Remove `ExpenseCategory` import
   - Load categories from API
   - Change category field to dropdown with dynamic categories
   - Use `categoryId` instead of `category` in form data

4. **page.tsx**
   - Remove `ExpenseCategory` import
   - Change filter state from `ExpenseCategory[]` to `string[]`
   - Add CategoryManagement component to the page

## Database Schema Changes

### New Table: `categories`
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Modified Tables:
- `expenses` - Added `category_id` foreign key (removed `category` enum column)
- `recurring_expenses` - Added `category_id` foreign key (removed `category` enum column)

## Default Categories
The following default categories are seeded automatically:
1. Groceries - Food and household items
2. Transportation - Gas, public transit, parking
3. Entertainment - Movies, games, subscriptions
4. Utilities - Electricity, water, internet
5. Other - Miscellaneous expenses

Default categories cannot be edited or deleted through the UI.

## Next Steps
1. Update remaining frontend components (ExpenseForm, ExpenseFilter, RecurringExpenses, page.tsx)
2. Test the full flow: create category → create expense with that category → filter by category
3. Handle edge cases (deleting a category that has expenses)
