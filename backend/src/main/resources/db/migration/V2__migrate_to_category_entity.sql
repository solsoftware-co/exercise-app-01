-- Migration to convert from ExpenseCategory enum to Category entity

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Insert default categories
INSERT INTO categories (name, description, is_default, created_at, updated_at)
VALUES 
    ('Groceries', 'Food and household items', true, NOW(), NOW()),
    ('Transportation', 'Gas, public transit, parking', true, NOW(), NOW()),
    ('Entertainment', 'Movies, games, subscriptions', true, NOW(), NOW()),
    ('Utilities', 'Electricity, water, internet', true, NOW(), NOW()),
    ('Other', 'Miscellaneous expenses', true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Add new category_id column to expenses table
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- Migrate existing data from category enum to category_id
UPDATE expenses SET category_id = (
    SELECT id FROM categories WHERE UPPER(categories.name) = expenses.category
);

-- Drop old category column and add foreign key constraint
ALTER TABLE expenses DROP COLUMN IF EXISTS category;
ALTER TABLE expenses ADD CONSTRAINT fk_expenses_category 
    FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE expenses ALTER COLUMN category_id SET NOT NULL;

-- Add new category_id column to recurring_expenses table
ALTER TABLE recurring_expenses ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- Migrate existing data from category enum to category_id
UPDATE recurring_expenses SET category_id = (
    SELECT id FROM categories WHERE UPPER(categories.name) = recurring_expenses.category
);

-- Drop old category column and add foreign key constraint
ALTER TABLE recurring_expenses DROP COLUMN IF EXISTS category;
ALTER TABLE recurring_expenses ADD CONSTRAINT fk_recurring_expenses_category 
    FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE recurring_expenses ALTER COLUMN category_id SET NOT NULL;
