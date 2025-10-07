"use client"

import { useState, useEffect } from "react"
import type { FormEvent } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { budgetApi, type BudgetStatus } from "@/lib/api"
import { formatCurrency } from "@/lib/utils"
import { useToast } from "@/components/ui/use-toast"
import { AlertCircle, CheckCircle, TrendingUp, DollarSign } from "lucide-react"

interface BudgetTrackerProps {
  onBudgetChange: () => void
}

export function BudgetTracker({ onBudgetChange }: BudgetTrackerProps) {
  const { toast } = useToast()
  const [budgetStatus, setBudgetStatus] = useState<BudgetStatus | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [monthlyLimit, setMonthlyLimit] = useState<string>("")
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadBudgetStatus()
  }, [])

  const loadBudgetStatus = async () => {
    try {
      const status = await budgetApi.getBudgetStatus()
      setBudgetStatus(status)
      setMonthlyLimit(status.monthlyLimit.toString())
    } catch (error: any) {
      // Budget not set yet
      if (error.response?.status === 404) {
        setIsEditing(true)
      }
    }
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)

    try {
      const limit = parseFloat(monthlyLimit)
      if (isNaN(limit) || limit <= 0) {
        toast({
          title: "Invalid Amount",
          description: "Please enter a valid budget amount",
          variant: "destructive",
        })
        return
      }

      await budgetApi.setBudget({ monthlyLimit: limit })
      await loadBudgetStatus()
      setIsEditing(false)
      onBudgetChange()
      
      toast({
        title: "Success",
        description: "Budget updated successfully",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update budget",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = () => {
    if (!budgetStatus) return "bg-gray-500"
    switch (budgetStatus.status) {
      case "HEALTHY":
        return "bg-green-500"
      case "WARNING":
        return "bg-yellow-500"
      case "OVER_BUDGET":
        return "bg-red-500"
      default:
        return "bg-gray-500"
    }
  }

  const getStatusIcon = () => {
    if (!budgetStatus) return null
    switch (budgetStatus.status) {
      case "HEALTHY":
        return <CheckCircle className="h-5 w-5 text-green-600" />
      case "WARNING":
        return <AlertCircle className="h-5 w-5 text-yellow-600" />
      case "OVER_BUDGET":
        return <AlertCircle className="h-5 w-5 text-red-600" />
    }
  }

  const getStatusMessage = () => {
    if (!budgetStatus) return ""
    switch (budgetStatus.status) {
      case "HEALTHY":
        return "You're on track!"
      case "WARNING":
        return "Approaching budget limit"
      case "OVER_BUDGET":
        return "Over budget!"
    }
  }

  if (isEditing || !budgetStatus) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Set Monthly Budget</CardTitle>
          <CardDescription>
            {budgetStatus ? "Update your monthly spending limit" : "Set a monthly spending limit to track your budget"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="budget">Monthly Budget ($)</Label>
              <Input
                id="budget"
                type="number"
                step="0.01"
                min="0.01"
                required
                value={monthlyLimit}
                onChange={(e) => setMonthlyLimit(e.target.value)}
                placeholder="Enter your monthly budget"
              />
            </div>
            <div className="flex gap-2">
              <Button type="submit" disabled={loading}>
                {loading ? "Saving..." : "Save Budget"}
              </Button>
              {budgetStatus && (
                <Button type="button" variant="outline" onClick={() => setIsEditing(false)}>
                  Cancel
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Budget Tracker</CardTitle>
          <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
            Edit Budget
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Budget Overview */}
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="h-4 w-4" />
              <span>Monthly Budget</span>
            </div>
            <div className="text-2xl font-bold">
              {formatCurrency(budgetStatus.monthlyLimit)}
            </div>
          </div>
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <TrendingUp className="h-4 w-4" />
              <span>Total Spent</span>
            </div>
            <div className="text-2xl font-bold">
              {formatCurrency(budgetStatus.totalSpent)}
            </div>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="font-medium">Budget Usage</span>
            <span className="font-bold">{budgetStatus.percentageUsed.toFixed(1)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
            <div
              className={`h-full transition-all duration-500 ${getStatusColor()}`}
              style={{ width: `${Math.min(budgetStatus.percentageUsed, 100)}%` }}
            />
          </div>
        </div>

        {/* Remaining Budget */}
        <div className={`p-4 rounded-lg ${
          budgetStatus.status === "HEALTHY" ? "bg-green-50" :
          budgetStatus.status === "WARNING" ? "bg-yellow-50" :
          "bg-red-50"
        }`}>
          <div className="flex items-center gap-3">
            {getStatusIcon()}
            <div className="flex-1">
              <div className="font-semibold text-sm">
                {getStatusMessage()}
              </div>
              <div className="text-2xl font-bold mt-1">
                {budgetStatus.remaining >= 0 ? (
                  <>
                    {formatCurrency(budgetStatus.remaining)} <span className="text-sm font-normal">remaining</span>
                  </>
                ) : (
                  <>
                    {formatCurrency(Math.abs(budgetStatus.remaining))} <span className="text-sm font-normal">over budget</span>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Warning Messages */}
        {budgetStatus.status === "WARNING" && (
          <div className="text-sm text-yellow-700 bg-yellow-50 p-3 rounded-md border border-yellow-200">
            ⚠️ You&apos;ve used {budgetStatus.percentageUsed.toFixed(1)}% of your budget. Consider reducing spending.
          </div>
        )}
        {budgetStatus.status === "OVER_BUDGET" && (
          <div className="text-sm text-red-700 bg-red-50 p-3 rounded-md border border-red-200">
            🚨 You&apos;ve exceeded your budget by {formatCurrency(Math.abs(budgetStatus.remaining))}. Review your expenses.
          </div>
        )}
      </CardContent>
    </Card>
  )
}
