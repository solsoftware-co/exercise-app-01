import axios from 'axios'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

export interface Expense {
  id: number
  amount: number
  category: ExpenseCategory
  date: string
  description?: string
  createdAt: string
  updatedAt: string
}

export enum ExpenseCategory {
  GROCERIES = 'GROCERIES',
  TRANSPORTATION = 'TRANSPORTATION',
  ENTERTAINMENT = 'ENTERTAINMENT',
  UTILITIES = 'UTILITIES',
  OTHER = 'OTHER',
}

export interface ExpenseRequest {
  amount: number
  category: ExpenseCategory
  date: string
  description?: string
}

export interface CategorySummary {
  category: ExpenseCategory
  total: number
}

export interface MonthlySummary {
  total: number
  month: number
  year: number
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
