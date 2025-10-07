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
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { expenseApi, categoryApi, type Expense, type ExpenseRequest, type Category } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"

interface ExpenseFormProps {
  expense?: Expense
  onSuccess: () => void
  onCancel?: () => void
}

export function ExpenseForm({ expense, onSuccess, onCancel }: ExpenseFormProps) {
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Category[]>([])
  const [formData, setFormData] = useState<ExpenseRequest>({
    amount: 0,
    categoryId: 0,
    date: new Date().toISOString().split('T')[0],
    description: '',
  })

  useEffect(() => {
    loadCategories()
  }, [])

  useEffect(() => {
    if (expense && categories.length > 0) {
      const category = categories.find(c => c.name === expense.category)
      setFormData({
        amount: expense.amount,
        categoryId: category?.id || categories[0]?.id || 0,
        date: expense.date,
        description: expense.description || '',
      })
    }
  }, [expense, categories])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.getAllCategories()
      setCategories(data.sort((a, b) => a.name.localeCompare(b.name)))
      // Set default category if creating new expense
      if (!expense && data.length > 0) {
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

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (expense) {
        await expenseApi.updateExpense(expense.id, formData)
        toast({
          title: "Success",
          description: "Expense updated successfully",
        })
      } else {
        await expenseApi.createExpense(formData)
        toast({
          title: "Success",
          description: "Expense created successfully",
        })
        setFormData({
          amount: 0,
          categoryId: categories[0]?.id || 0,
          date: new Date().toISOString().split('T')[0],
          description: '',
        })
      }
      onSuccess()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to save expense",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{expense ? 'Edit Expense' : 'Add New Expense'}</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="amount">Amount ($)</Label>
            <Input
              id="amount"
              type="number"
              step="0.01"
              min="0.01"
              required
              value={formData.amount || ''}
              onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) })}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">Category</Label>
            <Select
              value={formData.categoryId.toString()}
              onValueChange={(value) => setFormData({ ...formData, categoryId: parseInt(value) })}
            >
              <SelectTrigger id="category">
                <SelectValue placeholder="Select a category" />
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
            <Label htmlFor="date">Date</Label>
            <Input
              id="date"
              type="date"
              required
              value={formData.date}
              onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description (Optional)</Label>
            <Input
              id="description"
              type="text"
              maxLength={500}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Add a note about this expense"
            />
          </div>

          <div className="flex gap-2">
            <Button type="submit" disabled={loading}>
              {loading ? 'Saving...' : expense ? 'Update' : 'Add Expense'}
            </Button>
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel}>
                Cancel
              </Button>
            )}
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
