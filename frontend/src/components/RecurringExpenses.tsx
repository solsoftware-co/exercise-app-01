"use client"

import { useState, useEffect } from "react"
import type { FormEvent } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { recurringExpenseApi, categoryApi, RecurrenceFrequency, type RecurringExpense, type RecurringExpenseRequest, type Category } from "@/lib/api"
import { formatCurrency, formatDate } from "@/lib/utils"
import { useToast } from "@/components/ui/use-toast"
import { Plus, Repeat, Trash2, Power, PowerOff, Edit } from "lucide-react"

interface RecurringExpensesProps {
  onUpdate: () => void
}

export function RecurringExpenses({ onUpdate }: RecurringExpensesProps) {
  const { toast } = useToast()
  const [recurringExpenses, setRecurringExpenses] = useState<RecurringExpense[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<RecurringExpenseRequest>({
    amount: 0,
    categoryId: 0,
    description: '',
    frequency: RecurrenceFrequency.MONTHLY,
    startDate: new Date().toISOString().split('T')[0],
    endDate: undefined,
  })

  useEffect(() => {
    loadRecurringExpenses()
    loadCategories()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const loadRecurringExpenses = async () => {
    try {
      const data = await recurringExpenseApi.getAllRecurringExpenses()
      setRecurringExpenses(data.sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      ))
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load recurring expenses",
        variant: "destructive",
      })
    }
  }

  const loadCategories = async () => {
    try {
      const data = await categoryApi.getAllCategories()
      setCategories(data.sort((a, b) => a.name.localeCompare(b.name)))
      if (data.length > 0 && formData.categoryId === 0) {
        setFormData(prev => ({ ...prev, categoryId: data[0].id }))
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load categories",
        variant: "destructive",
      })
    }
  }

  const handleOpenDialog = () => {
    setIsEditing(false)
    setEditingId(null)
    setFormData({
      amount: 0,
      categoryId: categories[0]?.id || 0,
      description: '',
      frequency: RecurrenceFrequency.MONTHLY,
      startDate: new Date().toISOString().split('T')[0],
      endDate: undefined,
    })
    setIsDialogOpen(true)
  }

  const handleEdit = (expense: RecurringExpense) => {
    const category = categories.find(c => c.name === expense.category)
    setIsEditing(true)
    setEditingId(expense.id)
    setFormData({
      amount: expense.amount,
      categoryId: category?.id || categories[0]?.id || 0,
      description: expense.description || '',
      frequency: expense.frequency,
      startDate: expense.startDate,
      endDate: expense.endDate || undefined,
    })
    setIsDialogOpen(true)
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (isEditing && editingId) {
        await recurringExpenseApi.updateRecurringExpense(editingId, formData)
        toast({
          title: "Success",
          description: "Recurring expense updated successfully",
        })
      } else {
        await recurringExpenseApi.createRecurringExpense(formData)
        toast({
          title: "Success",
          description: "Recurring expense created successfully",
        })
      }
      setIsDialogOpen(false)
      setIsEditing(false)
      setEditingId(null)
      setFormData({
        amount: 0,
        categoryId: categories[0]?.id || 0,
        description: '',
        frequency: RecurrenceFrequency.MONTHLY,
        startDate: new Date().toISOString().split('T')[0],
        endDate: undefined,
      })
      loadRecurringExpenses()
      onUpdate()
    } catch (error) {
      toast({
        title: "Error",
        description: `Failed to ${isEditing ? 'update' : 'create'} recurring expense`,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleToggle = async (id: number, currentActive: boolean) => {
    const newActive = !currentActive
    
    // Optimistically update the UI
    setRecurringExpenses(prev => 
      prev.map(expense => 
        expense.id === id ? { ...expense, active: newActive } : expense
      )
    )
    
    try {
      await recurringExpenseApi.toggleRecurringExpense(id, newActive)
      toast({
        title: "Success",
        description: `Recurring expense ${newActive ? 'activated' : 'deactivated'}`,
      })
    } catch (error) {
      // Revert the optimistic update on error
      setRecurringExpenses(prev => 
        prev.map(expense => 
          expense.id === id ? { ...expense, active: currentActive } : expense
        )
      )
      toast({
        title: "Error",
        description: "Failed to toggle recurring expense",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this recurring expense?')) return

    try {
      await recurringExpenseApi.deleteRecurringExpense(id)
      toast({
        title: "Success",
        description: "Recurring expense deleted successfully",
      })
      loadRecurringExpenses()
      onUpdate()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete recurring expense",
        variant: "destructive",
      })
    }
  }

  const formatFrequency = (frequency: RecurrenceFrequency) => {
    return frequency.charAt(0) + frequency.slice(1).toLowerCase()
  }

  const getFrequencyColor = (frequency: RecurrenceFrequency) => {
    const colors: Record<RecurrenceFrequency, string> = {
      DAILY: 'bg-purple-100 text-purple-800',
      WEEKLY: 'bg-blue-100 text-blue-800',
      BIWEEKLY: 'bg-cyan-100 text-cyan-800',
      MONTHLY: 'bg-green-100 text-green-800',
      QUARTERLY: 'bg-yellow-100 text-yellow-800',
      YEARLY: 'bg-red-100 text-red-800',
    }
    return colors[frequency]
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Recurring Expenses</CardTitle>
            <CardDescription>Automatically create expenses on a schedule</CardDescription>
          </div>
          <Button onClick={handleOpenDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Add Recurring
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {recurringExpenses.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Repeat className="h-12 w-12 mx-auto mb-3 opacity-50" />
            <p>No recurring expenses yet</p>
            <p className="text-sm">Add one to automatically track regular expenses</p>
          </div>
        ) : (
          <div className="space-y-3">
            {recurringExpenses.map((expense) => (
              <div
                key={expense.id}
                className={`p-4 border rounded-lg ${expense.active ? '' : 'opacity-50 bg-muted/30'}`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className="text-lg font-bold">
                        {formatCurrency(expense.amount)}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getFrequencyColor(expense.frequency)}`}>
                        {formatFrequency(expense.frequency)}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {expense.category}
                      </span>
                    </div>
                    {expense.description && (
                      <p className="text-sm mb-2">{expense.description}</p>
                    )}
                    <div className="flex items-center gap-4 text-xs text-muted-foreground">
                      <span>Next: {formatDate(expense.nextOccurrence)}</span>
                      {expense.endDate && (
                        <span>Ends: {formatDate(expense.endDate)}</span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleEdit(expense)}
                      title="Edit"
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleToggle(expense.id, expense.active)}
                      title={expense.active ? 'Deactivate' : 'Activate'}
                    >
                      {expense.active ? (
                        <Power className="h-4 w-4 text-green-600" />
                      ) : (
                        <PowerOff className="h-4 w-4 text-gray-400" />
                      )}
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleDelete(expense.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>

      {/* Add Recurring Expense Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Edit' : 'Add'} Recurring Expense</DialogTitle>
            <DialogDescription>
              {isEditing ? 'Update the recurring expense details' : 'Set up an expense that repeats automatically'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="recurring-amount">Amount ($)</Label>
              <Input
                id="recurring-amount"
                type="number"
                step="0.01"
                min="0.01"
                required
                value={formData.amount || ''}
                onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="recurring-category">Category</Label>
              <Select
                value={formData.categoryId.toString()}
                onValueChange={(value) => setFormData({ ...formData, categoryId: parseInt(value) })}
              >
                <SelectTrigger id="recurring-category">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={category.id.toString()}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="recurring-frequency">Frequency</Label>
              <Select
                value={formData.frequency}
                onValueChange={(value) => setFormData({ ...formData, frequency: value as RecurrenceFrequency })}
              >
                <SelectTrigger id="recurring-frequency">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={RecurrenceFrequency.DAILY}>Daily</SelectItem>
                  <SelectItem value={RecurrenceFrequency.WEEKLY}>Weekly</SelectItem>
                  <SelectItem value={RecurrenceFrequency.BIWEEKLY}>Bi-weekly</SelectItem>
                  <SelectItem value={RecurrenceFrequency.MONTHLY}>Monthly</SelectItem>
                  <SelectItem value={RecurrenceFrequency.QUARTERLY}>Quarterly</SelectItem>
                  <SelectItem value={RecurrenceFrequency.YEARLY}>Yearly</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="recurring-description">Description (Optional)</Label>
              <Input
                id="recurring-description"
                type="text"
                maxLength={500}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="e.g., Monthly rent"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="recurring-start-date">Start Date</Label>
                <Input
                  id="recurring-start-date"
                  type="date"
                  required
                  value={formData.startDate}
                  onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="recurring-end-date">End Date (Optional)</Label>
                <Input
                  id="recurring-end-date"
                  type="date"
                  value={formData.endDate || ''}
                  onChange={(e) => setFormData({ ...formData, endDate: e.target.value || undefined })}
                />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? (isEditing ? 'Updating...' : 'Creating...') : (isEditing ? 'Update Recurring Expense' : 'Create Recurring Expense')}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
