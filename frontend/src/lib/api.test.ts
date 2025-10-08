import axios from 'axios'
import { RecurrenceFrequency } from './api'

// Mock axios with factory function
jest.mock('axios', () => {
  const mockInstance = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn()
  }
  
  return {
    __esModule: true,
    default: {
      create: jest.fn(() => mockInstance)
    }
  }
})

// Import API after mocking
const { expenseApi, budgetApi, categoryApi, recurringExpenseApi } = require('./api')

// Get reference to the mock instance
const mockAxiosInstance = (axios.create as jest.Mock).mock.results[0].value

describe('API Tests', () => {
  beforeEach(() => {
    // Clear mock calls between tests
    jest.clearAllMocks()
  })

  describe('expenseApi', () => {
    it('should get all expenses', async () => {
      const mockExpenses = [
        { id: 1, amount: 50, category: 'Groceries', date: '2025-01-01', createdAt: '', updatedAt: '' }
      ]
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockExpenses })

      const result = await expenseApi.getAllExpenses()

      expect(result).toEqual(mockExpenses)
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/expenses')
    })

    it('should get filtered expenses', async () => {
      const mockExpenses = [
        { id: 1, amount: 50, category: 'Groceries', date: '2025-01-01', createdAt: '', updatedAt: '' }
      ]
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockExpenses })

      const result = await expenseApi.getFilteredExpenses(['Groceries'], '2025-01-01', '2025-12-31')

      expect(result).toEqual(mockExpenses)
      expect(mockAxiosInstance.get).toHaveBeenCalled()
    })

    it('should create expense', async () => {
      const mockExpense = { id: 1, amount: 50, category: 'Groceries', date: '2025-01-01', createdAt: '', updatedAt: '' }
      const request = { amount: 50, categoryId: 1, date: '2025-01-01' }
      mockAxiosInstance.post.mockResolvedValueOnce({ data: mockExpense })

      const result = await expenseApi.createExpense(request)

      expect(result).toEqual(mockExpense)
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/expenses', request)
    })

    it('should update expense', async () => {
      const mockExpense = { id: 1, amount: 100, category: 'Groceries', date: '2025-01-01', createdAt: '', updatedAt: '' }
      const request = { amount: 100, categoryId: 1, date: '2025-01-01' }
      mockAxiosInstance.put.mockResolvedValueOnce({ data: mockExpense })

      const result = await expenseApi.updateExpense(1, request)

      expect(result).toEqual(mockExpense)
      expect(mockAxiosInstance.put).toHaveBeenCalledWith('/expenses/1', request)
    })

    it('should delete expense', async () => {
      mockAxiosInstance.delete.mockResolvedValueOnce({})

      await expenseApi.deleteExpense(1)

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith('/expenses/1')
    })
  })

  describe('budgetApi', () => {
    it('should get budget', async () => {
      const mockBudget = { id: 1, monthlyLimit: 2000, createdAt: '', updatedAt: '' }
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockBudget })

      const result = await budgetApi.getBudget()

      expect(result).toEqual(mockBudget)
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/budget')
    })

    it('should set budget', async () => {
      const mockBudget = { id: 1, monthlyLimit: 2000, createdAt: '', updatedAt: '' }
      const request = { monthlyLimit: 2000 }
      mockAxiosInstance.post.mockResolvedValueOnce({ data: mockBudget })

      const result = await budgetApi.setBudget(request)

      expect(result).toEqual(mockBudget)
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/budget', request)
    })

    it('should get budget status', async () => {
      const mockStatus = {
        monthlyLimit: 2000,
        totalSpent: 1000,
        remaining: 1000,
        percentageUsed: 50,
        status: 'HEALTHY' as const
      }
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockStatus })

      const result = await budgetApi.getBudgetStatus()

      expect(result).toEqual(mockStatus)
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/budget/status')
    })
  })

  describe('categoryApi', () => {
    it('should get all categories', async () => {
      const mockCategories = [
        { id: 1, name: 'Groceries', isDefault: true, createdAt: '', updatedAt: '' }
      ]
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockCategories })

      const result = await categoryApi.getAllCategories()

      expect(result).toEqual(mockCategories)
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/categories')
    })

    it('should create category', async () => {
      const mockCategory = { id: 1, name: 'Custom', isDefault: false, createdAt: '', updatedAt: '' }
      const request = { name: 'Custom', description: 'Test' }
      mockAxiosInstance.post.mockResolvedValueOnce({ data: mockCategory })

      const result = await categoryApi.createCategory(request)

      expect(result).toEqual(mockCategory)
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/categories', request)
    })

    it('should delete category', async () => {
      mockAxiosInstance.delete.mockResolvedValueOnce({})

      await categoryApi.deleteCategory(1)

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith('/categories/1')
    })
  })

  describe('recurringExpenseApi', () => {
    it('should get all recurring expenses', async () => {
      const mockExpenses = [
        {
          id: 1,
          amount: 100,
          category: 'Utilities',
          frequency: RecurrenceFrequency.MONTHLY,
          startDate: '2025-01-01',
          nextOccurrence: '2025-02-01',
          active: true,
          createdAt: '',
          updatedAt: ''
        }
      ]
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockExpenses })

      const result = await recurringExpenseApi.getAllRecurringExpenses()

      expect(result).toEqual(mockExpenses)
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/recurring-expenses')
    })

    it('should toggle recurring expense', async () => {
      mockAxiosInstance.patch.mockResolvedValueOnce({})

      await recurringExpenseApi.toggleRecurringExpense(1, false)

      expect(mockAxiosInstance.patch).toHaveBeenCalledWith('/recurring-expenses/1/toggle?active=false')
    })
  })
})
