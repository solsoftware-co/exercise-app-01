"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { type CategorySummary, type MonthlySummary } from "@/lib/api"
import { formatCurrency } from "@/lib/utils"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts"

interface SpendingSummaryProps {
  categorySummary: CategorySummary[]
  monthlySummary: MonthlySummary
}

const CATEGORY_COLORS: Record<string, string> = {
  GROCERIES: '#10b981',
  TRANSPORTATION: '#3b82f6',
  ENTERTAINMENT: '#8b5cf6',
  UTILITIES: '#f59e0b',
  OTHER: '#6b7280',
}

export function SpendingSummary({ categorySummary, monthlySummary }: SpendingSummaryProps) {
  const formatCategory = (category: string) => {
    return category.charAt(0) + category.slice(1).toLowerCase()
  }

  const chartData = categorySummary.map(item => ({
    category: formatCategory(item.category),
    total: item.total,
    color: CATEGORY_COLORS[item.category] || CATEGORY_COLORS.OTHER,
  }))

  const getMonthName = (month: number) => {
    const date = new Date(2024, month - 1, 1)
    return date.toLocaleString('en-US', { month: 'long' })
  }

  return (
    <div className="space-y-6">
      {/* Monthly Total */}
      <Card>
        <CardHeader>
          <CardTitle>Monthly Total</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <div className="text-4xl font-bold text-primary">
              {formatCurrency(monthlySummary.total)}
            </div>
            <div className="text-sm text-muted-foreground">
              {getMonthName(monthlySummary.month)} {monthlySummary.year}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Category Breakdown */}
      <Card>
        <CardHeader>
          <CardTitle>Spending by Category</CardTitle>
        </CardHeader>
        <CardContent>
          {categorySummary.length === 0 ? (
            <p className="text-muted-foreground text-center py-8">
              No spending data available yet.
            </p>
          ) : (
            <div className="space-y-6">
              {/* Category List */}
              <div className="space-y-3">
                {categorySummary.map((item) => (
                  <div key={item.category} className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div
                        className="w-4 h-4 rounded"
                        style={{ backgroundColor: CATEGORY_COLORS[item.category] }}
                      />
                      <span className="font-medium">{formatCategory(item.category)}</span>
                    </div>
                    <span className="font-bold">{formatCurrency(item.total)}</span>
                  </div>
                ))}
              </div>

              {/* Bar Chart */}
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="category" />
                    <YAxis />
                    <Tooltip
                      formatter={(value: number) => formatCurrency(value)}
                      contentStyle={{
                        backgroundColor: 'hsl(var(--background))',
                        border: '1px solid hsl(var(--border))',
                        borderRadius: '6px',
                      }}
                    />
                    <Bar dataKey="total" radius={[8, 8, 0, 0]}>
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
