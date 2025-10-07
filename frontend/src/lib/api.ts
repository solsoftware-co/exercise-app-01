import axios from 'axios'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

export interface Expense {
  id: number
  amount: number
  category: string
  date: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  description?: string
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface CategoryRequest {
  name: string
  description?: string
}

export interface ExpenseRequest {
  amount: number
  categoryId: number
  date: string
  description?: string
}

export interface CategorySummary {
  category: string
  total: number
}

export interface MonthlySummary {
  total: number
  month: number
  year: number
}

export interface Budget {
  id: number
  monthlyLimit: number
  createdAt: string
  updatedAt: string
}

export interface BudgetRequest {
  monthlyLimit: number
}

export interface BudgetStatus {
  monthlyLimit: number
  totalSpent: number
  remaining: number
  percentageUsed: number
  status: 'HEALTHY' | 'WARNING' | 'OVER_BUDGET'
}

export interface RecurringExpense {
  id: number
  amount: number
  category: string
  description?: string
  frequency: RecurrenceFrequency
  startDate: string
  endDate?: string
  nextOccurrence: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface RecurringExpenseRequest {
  amount: number
  categoryId: number
  description?: string
  frequency: RecurrenceFrequency
  startDate: string
  endDate?: string
}

export enum RecurrenceFrequency {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  BIWEEKLY = 'BIWEEKLY',
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  YEARLY = 'YEARLY',
}

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export const expenseApi = {
  getAllExpenses: async (): Promise<Expense[]> => {
    const response = await api.get<Expense[]>('/expenses')
    return response.data
  },

  getFilteredExpenses: async (categories?: string[], startDate?: string, endDate?: string): Promise<Expense[]> => {
    const params = new URLSearchParams()
    if (categories && categories.length > 0) {
      categories.forEach(cat => params.append('categories', cat))
    }
    if (startDate) params.append('startDate', startDate)
    if (endDate) params.append('endDate', endDate)
    
    const response = await api.get<Expense[]>(`/expenses/filter?${params.toString()}`)
    return response.data
  },

  getExpenseById: async (id: number): Promise<Expense> => {
    const response = await api.get<Expense>(`/expenses/${id}`)
    return response.data
  },

  createExpense: async (expense: ExpenseRequest): Promise<Expense> => {
    const response = await api.post<Expense>('/expenses', expense)
    return response.data
  },

  updateExpense: async (id: number, expense: ExpenseRequest): Promise<Expense> => {
    const response = await api.put<Expense>(`/expenses/${id}`, expense)
    return response.data
  },

  deleteExpense: async (id: number): Promise<void> => {
    await api.delete(`/expenses/${id}`)
  },

  getCategorySummary: async (): Promise<CategorySummary[]> => {
    const response = await api.get<CategorySummary[]>('/expenses/summary/by-category')
    return response.data
  },

  getMonthlySummary: async (): Promise<MonthlySummary> => {
    const response = await api.get<MonthlySummary>('/expenses/summary/monthly')
    return response.data
  },
}

export const budgetApi = {
  getBudget: async (): Promise<Budget> => {
    const response = await api.get<Budget>('/budget')
    return response.data
  },

  setBudget: async (budget: BudgetRequest): Promise<Budget> => {
    const response = await api.post<Budget>('/budget', budget)
    return response.data
  },

  getBudgetStatus: async (): Promise<BudgetStatus> => {
    const response = await api.get<BudgetStatus>('/budget/status')
    return response.data
  },
}

export const categoryApi = {
  getAllCategories: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>('/categories')
    return response.data
  },

  getCategoryById: async (id: number): Promise<Category> => {
    const response = await api.get<Category>(`/categories/${id}`)
    return response.data
  },

  createCategory: async (category: CategoryRequest): Promise<Category> => {
    const response = await api.post<Category>('/categories', category)
    return response.data
  },

  updateCategory: async (id: number, category: CategoryRequest): Promise<Category> => {
    const response = await api.put<Category>(`/categories/${id}`, category)
    return response.data
  },

  deleteCategory: async (id: number): Promise<void> => {
    await api.delete(`/categories/${id}`)
  },
}

export const recurringExpenseApi = {
  getAllRecurringExpenses: async (): Promise<RecurringExpense[]> => {
    const response = await api.get<RecurringExpense[]>('/recurring-expenses')
    return response.data
  },

  getActiveRecurringExpenses: async (): Promise<RecurringExpense[]> => {
    const response = await api.get<RecurringExpense[]>('/recurring-expenses/active')
    return response.data
  },

  getRecurringExpenseById: async (id: number): Promise<RecurringExpense> => {
    const response = await api.get<RecurringExpense>(`/recurring-expenses/${id}`)
    return response.data
  },

  createRecurringExpense: async (expense: RecurringExpenseRequest): Promise<RecurringExpense> => {
    const response = await api.post<RecurringExpense>('/recurring-expenses', expense)
    return response.data
  },

  updateRecurringExpense: async (id: number, expense: RecurringExpenseRequest): Promise<RecurringExpense> => {
    const response = await api.put<RecurringExpense>(`/recurring-expenses/${id}`, expense)
    return response.data
  },

  deleteRecurringExpense: async (id: number): Promise<void> => {
    await api.delete(`/recurring-expenses/${id}`)
  },

  toggleRecurringExpense: async (id: number, active: boolean): Promise<void> => {
    await api.patch(`/recurring-expenses/${id}/toggle?active=${active}`)
  },

  processRecurringExpenses: async (): Promise<void> => {
    await api.post('/recurring-expenses/process')
  },
}
