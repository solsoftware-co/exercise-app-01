"use client"

import { useState, useEffect } from "react"
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
import { categoryApi, type Category } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"
import { Filter, X } from "lucide-react"

interface ExpenseFilterProps {
  onFilter: (categories: string[], startDate?: string, endDate?: string) => void
}

export function ExpenseFilter({ onFilter }: ExpenseFilterProps) {
  const { toast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])
  const [startDate, setStartDate] = useState<string>("")
  const [endDate, setEndDate] = useState<string>("")
  const [isExpanded, setIsExpanded] = useState(false)

  useEffect(() => {
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.getAllCategories()
      setCategories(data.sort((a, b) => a.name.localeCompare(b.name)))
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load categories",
        variant: "destructive",
      })
    }
  }

  const handleAddCategory = (category: string) => {
    if (category !== "placeholder" && !selectedCategories.includes(category)) {
      const newCategories = [...selectedCategories, category]
      setSelectedCategories(newCategories)
    }
  }

  const handleRemoveCategory = (category: string) => {
    const newCategories = selectedCategories.filter(c => c !== category)
    setSelectedCategories(newCategories)
    // Auto-apply filter when removing a category
    onFilter(newCategories, startDate || undefined, endDate || undefined)
  }

  const handleApplyFilter = () => {
    onFilter(selectedCategories, startDate || undefined, endDate || undefined)
  }

  const handleClearFilter = () => {
    setSelectedCategories([])
    setStartDate("")
    setEndDate("")
    onFilter([], undefined, undefined)
  }

  const hasActiveFilters = selectedCategories.length > 0 || startDate || endDate

  if (!isExpanded) {
    return (
      <Card>
        <CardContent className="pt-6">
          <Button
            variant="outline"
            className="w-full"
            onClick={() => setIsExpanded(true)}
          >
            <Filter className="h-4 w-4 mr-2" />
            Filter Expenses
            {hasActiveFilters && (
              <span className="ml-2 px-2 py-0.5 bg-primary text-primary-foreground rounded-full text-xs">
                Active
              </span>
            )}
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Filter Expenses</CardTitle>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setIsExpanded(false)}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Category Filter */}
        <div className="space-y-2">
          <Label htmlFor="filter-category">Categories</Label>
          <Select
            value="placeholder"
            onValueChange={handleAddCategory}
          >
            <SelectTrigger id="filter-category">
              <SelectValue placeholder="Select categories..." />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="placeholder" disabled>Select categories...</SelectItem>
              {categories.map((category) => (
                <SelectItem 
                  key={category.id} 
                  value={category.name}
                  disabled={selectedCategories.includes(category.name)}
                >
                  {category.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          
          {/* Selected Categories Display */}
          {selectedCategories.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-2">
              {selectedCategories.map((cat) => (
                <span
                  key={cat}
                  className="inline-flex items-center gap-1 px-3 py-1 bg-primary text-primary-foreground rounded-full text-sm"
                >
                  {cat}
                  <button
                    type="button"
                    onClick={() => handleRemoveCategory(cat)}
                    className="hover:bg-primary-foreground/20 rounded-full p-0.5"
                  >
                    <X className="h-3 w-3" />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        {/* Date Range Filter */}
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="filter-start-date">Start Date</Label>
            <div className="relative">
              <Input
                id="filter-start-date"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className={startDate ? "pr-8" : ""}
              />
              {startDate && (
                <button
                  type="button"
                  onClick={() => {
                    setStartDate("")
                    onFilter(selectedCategories, undefined, endDate || undefined)
                  }}
                  className="absolute right-2 top-1/2 -translate-y-1/2 hover:bg-accent rounded-full p-1"
                  title="Clear start date"
                >
                  <X className="h-4 w-4 text-muted-foreground hover:text-foreground" />
                </button>
              )}
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="filter-end-date">End Date</Label>
            <div className="relative">
              <Input
                id="filter-end-date"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className={endDate ? "pr-8" : ""}
              />
              {endDate && (
                <button
                  type="button"
                  onClick={() => {
                    setEndDate("")
                    onFilter(selectedCategories, startDate || undefined, undefined)
                  }}
                  className="absolute right-2 top-1/2 -translate-y-1/2 hover:bg-accent rounded-full p-1"
                  title="Clear end date"
                >
                  <X className="h-4 w-4 text-muted-foreground hover:text-foreground" />
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-2 pt-2">
          <Button onClick={handleApplyFilter} className="flex-1">
            <Filter className="h-4 w-4 mr-2" />
            Apply Filters
          </Button>
          {hasActiveFilters && (
            <Button variant="outline" onClick={handleClearFilter}>
              <X className="h-4 w-4 mr-2" />
              Clear
            </Button>
          )}
        </div>

        {/* Active Filters Display */}
        {hasActiveFilters && (
          <div className="pt-2 border-t">
            <p className="text-sm font-medium mb-2">Active Filters:</p>
            <div className="flex flex-wrap gap-2">
              {selectedCategories.length > 0 && (
                <span className="px-2 py-1 bg-primary/10 text-primary rounded-md text-xs">
                  Categories: {selectedCategories.length}
                </span>
              )}
              {startDate && (
                <span className="px-2 py-1 bg-primary/10 text-primary rounded-md text-xs">
                  From: {new Date(startDate).toLocaleDateString()}
                </span>
              )}
              {endDate && (
                <span className="px-2 py-1 bg-primary/10 text-primary rounded-md text-xs">
                  To: {new Date(endDate).toLocaleDateString()}
                </span>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
