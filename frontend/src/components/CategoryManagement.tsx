"use client"

import { useState, useEffect } from "react"
import type { FormEvent } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { categoryApi, type Category, type CategoryRequest } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"
import { Plus, Edit, Trash2, Tag } from "lucide-react"

interface CategoryManagementProps {
  onUpdate: () => void
}

export function CategoryManagement({ onUpdate }: CategoryManagementProps) {
  const { toast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [formData, setFormData] = useState<CategoryRequest>({
    name: '',
    description: '',
  })

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

  const handleOpenDialog = () => {
    setIsEditing(false)
    setEditingId(null)
    setFormData({ name: '', description: '' })
    setIsDialogOpen(true)
  }

  const handleEdit = (category: Category) => {
    setIsEditing(true)
    setEditingId(category.id)
    setFormData({
      name: category.name,
      description: category.description || '',
    })
    setIsDialogOpen(true)
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (isEditing && editingId) {
        await categoryApi.updateCategory(editingId, formData)
        toast({
          title: "Success",
          description: "Category updated successfully",
        })
      } else {
        await categoryApi.createCategory(formData)
        toast({
          title: "Success",
          description: "Category created successfully",
        })
      }
      setIsDialogOpen(false)
      setIsEditing(false)
      setEditingId(null)
      setFormData({ name: '', description: '' })
      loadCategories()
      onUpdate()
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || `Failed to ${isEditing ? 'update' : 'create'} category`,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number, isDefault: boolean) => {
    if (isDefault) {
      toast({
        title: "Cannot Delete",
        description: "Default categories cannot be deleted",
        variant: "destructive",
      })
      return
    }

    if (!confirm('Are you sure you want to delete this category?')) return

    try {
      await categoryApi.deleteCategory(id)
      toast({
        title: "Success",
        description: "Category deleted successfully",
      })
      loadCategories()
      onUpdate()
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to delete category",
        variant: "destructive",
      })
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Expense Categories</CardTitle>
            <CardDescription>Manage your custom expense categories</CardDescription>
          </div>
          <Button onClick={handleOpenDialog}>
            <Plus className="h-4 w-4 mr-2" />
            Add Category
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {categories.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Tag className="h-12 w-12 mx-auto mb-3 opacity-50" />
            <p>No categories yet</p>
            <p className="text-sm">Add one to organize your expenses</p>
          </div>
        ) : (
          <div className="space-y-2">
            {categories.map((category) => (
              <div
                key={category.id}
                className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent/50 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium">{category.name}</span>
                    {category.isDefault && (
                      <span className="px-2 py-0.5 rounded-full text-xs bg-blue-100 text-blue-800">
                        Default
                      </span>
                    )}
                  </div>
                  {category.description && (
                    <p className="text-sm text-muted-foreground mt-1">{category.description}</p>
                  )}
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => handleEdit(category)}
                    disabled={category.isDefault}
                    title={category.isDefault ? "Cannot edit default categories" : "Edit"}
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => handleDelete(category.id, category.isDefault)}
                    disabled={category.isDefault}
                    title={category.isDefault ? "Cannot delete default categories" : "Delete"}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>

      {/* Add/Edit Category Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Edit' : 'Add'} Category</DialogTitle>
            <DialogDescription>
              {isEditing ? 'Update the category details' : 'Create a new expense category'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="category-name">Name</Label>
              <Input
                id="category-name"
                type="text"
                required
                maxLength={50}
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g., Healthcare"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="category-description">Description (Optional)</Label>
              <Input
                id="category-description"
                type="text"
                maxLength={200}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="e.g., Medical expenses and insurance"
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? (isEditing ? 'Updating...' : 'Creating...') : (isEditing ? 'Update Category' : 'Create Category')}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
