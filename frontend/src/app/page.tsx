"use client"

import { useState, useEffect, useCallback } from "react"
import { ExpenseForm } from "@/components/ExpenseForm"
import { ExpenseList } from "@/components/ExpenseList"
import { SpendingSummary } from "@/components/SpendingSummary"
import { BudgetTracker } from "@/components/BudgetTracker"
import { ExpenseFilter } from "@/components/ExpenseFilter"
import { RecurringExpenses } from "@/components/RecurringExpenses"
import { expenseApi, type Expense, type CategorySummary, type MonthlySummary, ExpenseCategory } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"
import { Wallet } from "lucide-react"

export default function Home() {
  const { toast } = useToast()
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [categorySummary, setCategorySummary] = useState<CategorySummary[]>([])
  const [monthlySummary, setMonthlySummary] = useState<MonthlySummary>({
    total: 0,
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear(),
  })
  const [loading, setLoading] = useState(true)
  const [filterCategories, setFilterCategories] = useState<ExpenseCategory[]>([])
  const [filterStartDate, setFilterStartDate] = useState<string | undefined>(undefined)
  const [filterEndDate, setFilterEndDate] = useState<string | undefined>(undefined)

  const loadData = useCallback(async () => {
    try {
      const [expensesData, categoryData, monthlyData] = await Promise.all([
        expenseApi.getFilteredExpenses(filterCategories.length > 0 ? filterCategories : undefined, filterStartDate, filterEndDate),
        expenseApi.getCategorySummary(),
        expenseApi.getMonthlySummary(),
      ])

      setExpenses(expensesData.sort((a, b) => 
        new Date(b.date).getTime() - new Date(a.date).getTime()
      ))
      setCategorySummary(categoryData)
      setMonthlySummary(monthlyData)
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load expenses. Please check if the backend is running.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }, [toast, filterCategories, filterStartDate, filterEndDate])

  const handleFilter = useCallback((categories: ExpenseCategory[], startDate?: string, endDate?: string) => {
    setFilterCategories(categories)
    setFilterStartDate(startDate)
    setFilterEndDate(endDate)
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading expenses...</p>
        </div>
      </div>
    )
  }

  return (
    <main className="min-h-screen bg-gradient-to-b from-background to-muted/20">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <Wallet className="h-8 w-8 text-primary" />
            <h1 className="text-4xl font-bold">Expense Tracker</h1>
          </div>
          <p className="text-muted-foreground">
            Track your spending and understand your financial habits
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Form, Filter, and List */}
          <div className="lg:col-span-2 space-y-6">
            <ExpenseForm onSuccess={loadData} />
            <ExpenseFilter onFilter={handleFilter} />
            <RecurringExpenses onUpdate={loadData} />
            <ExpenseList expenses={expenses} onUpdate={loadData} />
          </div>

          {/* Right Column - Budget and Summary */}
          <div className="lg:col-span-1 space-y-6">
            <BudgetTracker onBudgetChange={loadData} />
            <SpendingSummary
              categorySummary={categorySummary}
              monthlySummary={monthlySummary}
            />
          </div>
        </div>
      </div>
    </main>
  )
}
