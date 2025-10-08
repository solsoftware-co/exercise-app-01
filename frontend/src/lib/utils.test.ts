import { cn, formatCurrency, formatDate, exportExpensesToCSV } from './utils'
import type { Expense } from './api'

describe('Utils', () => {
  describe('cn', () => {
    it('should merge class names', () => {
      const result = cn('px-2 py-1', 'px-4')
      expect(result).toBe('py-1 px-4')
    })

    it('should handle conditional classes', () => {
      const result = cn('base', true && 'active', false && 'disabled')
      expect(result).toBe('base active')
    })
  })

  describe('formatCurrency', () => {
    it('should format positive numbers as USD currency', () => {
      expect(formatCurrency(1234.56)).toBe('$1,234.56')
    })

    it('should format zero', () => {
      expect(formatCurrency(0)).toBe('$0.00')
    })

    it('should format negative numbers', () => {
      expect(formatCurrency(-50.99)).toBe('-$50.99')
    })

    it('should handle large numbers', () => {
      expect(formatCurrency(1000000)).toBe('$1,000,000.00')
    })
  })

  describe('formatDate', () => {
    it('should format string dates', () => {
      const result = formatDate('2025-01-15')
      expect(result).toMatch(/Jan 1[45], 2025/)
    })

    it('should format Date objects', () => {
      const date = new Date('2025-01-15')
      const result = formatDate(date)
      expect(result).toMatch(/Jan 1[45], 2025/)
    })
  })

  describe('exportExpensesToCSV', () => {
    let createElementSpy: jest.SpyInstance
    let appendChildSpy: jest.SpyInstance
    let removeChildSpy: jest.SpyInstance
    let clickSpy: jest.Mock

    beforeEach(() => {
      clickSpy = jest.fn()
      const mockLink = {
        setAttribute: jest.fn(),
        click: clickSpy,
        style: {},
      }
      
      createElementSpy = jest.spyOn(document, 'createElement').mockReturnValue(mockLink as any)
      appendChildSpy = jest.spyOn(document.body, 'appendChild').mockImplementation()
      removeChildSpy = jest.spyOn(document.body, 'removeChild').mockImplementation()
      
      global.URL.createObjectURL = jest.fn(() => 'blob:mock-url')
      global.URL.revokeObjectURL = jest.fn()
    })

    afterEach(() => {
      createElementSpy.mockRestore()
      appendChildSpy.mockRestore()
      removeChildSpy.mockRestore()
    })

    it('should export expenses to CSV', () => {
      const expenses: Expense[] = [
        {
          id: 1,
          amount: 50.00,
          category: 'Groceries',
          date: '2025-01-01',
          description: 'Weekly shopping',
          createdAt: '',
          updatedAt: ''
        },
        {
          id: 2,
          amount: 25.50,
          category: 'Transportation',
          date: '2025-01-02',
          createdAt: '',
          updatedAt: ''
        }
      ]

      exportExpensesToCSV(expenses, 'test.csv')

      expect(createElementSpy).toHaveBeenCalledWith('a')
      expect(clickSpy).toHaveBeenCalled()
      expect(global.URL.createObjectURL).toHaveBeenCalled()
      expect(global.URL.revokeObjectURL).toHaveBeenCalled()
    })

    it('should handle empty expenses array', () => {
      exportExpensesToCSV([], 'test.csv')

      expect(createElementSpy).not.toHaveBeenCalled()
    })

    it('should escape commas in CSV fields', () => {
      const expenses: Expense[] = [
        {
          id: 1,
          amount: 50.00,
          category: 'Food, Groceries',
          date: '2025-01-01',
          description: 'Test, with comma',
          createdAt: '',
          updatedAt: ''
        }
      ]

      exportExpensesToCSV(expenses)

      expect(clickSpy).toHaveBeenCalled()
    })
  })
})
