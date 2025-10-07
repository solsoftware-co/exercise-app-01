"use client"

import { useState } from "react"
import { Pencil, Trash2, Download } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { expenseApi, type Expense } from "@/lib/api"
import { formatCurrency, formatDate, exportExpensesToCSV } from "@/lib/utils"
import { useToast } from "@/components/ui/use-toast"
import { ExpenseForm } from "./ExpenseForm"

interface ExpenseListProps {
  expenses: Expense[]
  onUpdate: () => void
}

export function ExpenseList({ expenses, onUpdate }: ExpenseListProps) {
  const { toast } = useToast()
  const [editingExpense, setEditingExpense] = useState<Expense | null>(null)
  const [deletingExpense, setDeletingExpense] = useState<Expense | null>(null)
  const [loading, setLoading] = useState(false)

  const handleDelete = async () => {
    if (!deletingExpense) return

    setLoading(true)
    try {
      await expenseApi.deleteExpense(deletingExpense.id)
      toast({
        title: "Success",
        description: "Expense deleted successfully",
      })
      setDeletingExpense(null)
      onUpdate()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete expense",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const getCategoryColor = (category: string) => {
    const colors: Record<string, string> = {
      GROCERIES: 'bg-green-100 text-green-800',
      TRANSPORTATION: 'bg-blue-100 text-blue-800',
      ENTERTAINMENT: 'bg-purple-100 text-purple-800',
      UTILITIES: 'bg-yellow-100 text-yellow-800',
      OTHER: 'bg-gray-100 text-gray-800',
    }
    return colors[category] || colors.OTHER
  }

  const formatCategory = (category: string) => {
    return category.charAt(0) + category.slice(1).toLowerCase()
  }

  const handleExport = () => {
    const timestamp = new Date().toISOString().split('T')[0]
    exportExpensesToCSV(expenses, `expenses-${timestamp}.csv`)
    toast({
      title: "Success",
      description: `Exported ${expenses.length} expense${expenses.length === 1 ? '' : 's'} to CSV`,
    })
  }

  if (expenses.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Expenses</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-center py-8">
            No expenses yet. Add your first expense above!
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Expenses</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={handleExport}
              disabled={expenses.length === 0}
            >
              <Download className="h-4 w-4 mr-2" />
              Export CSV
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {expenses.map((expense) => (
              <div
                key={expense.id}
                className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent/50 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <span className="text-2xl font-bold">
                      {formatCurrency(expense.amount)}
                    </span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getCategoryColor(expense.category)}`}>
                      {formatCategory(expense.category)}
                    </span>
                  </div>
                  <div className="text-sm text-muted-foreground">
                    {formatDate(expense.date)}
                  </div>
                  {expense.description && (
                    <div className="text-sm mt-1">{expense.description}</div>
                  )}
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setEditingExpense(expense)}
                  >
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setDeletingExpense(expense)}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Edit Dialog */}
      <Dialog open={!!editingExpense} onOpenChange={(open) => !open && setEditingExpense(null)}>
        <DialogContent className="max-w-2xl">
          {editingExpense && (
            <ExpenseForm
              expense={editingExpense}
              onSuccess={() => {
                setEditingExpense(null)
                onUpdate()
              }}
              onCancel={() => setEditingExpense(null)}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deletingExpense} onOpenChange={(open) => !open && setDeletingExpense(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Expense</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this expense? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          {deletingExpense && (
            <div className="py-4">
              <div className="font-semibold">{formatCurrency(deletingExpense.amount)}</div>
              <div className="text-sm text-muted-foreground">
                {formatCategory(deletingExpense.category)} - {formatDate(deletingExpense.date)}
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeletingExpense(null)} disabled={loading}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDelete} disabled={loading}>
              {loading ? 'Deleting...' : 'Delete'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
