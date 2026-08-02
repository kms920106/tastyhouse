'use client'

import { getFaqCategories, getFaqList } from '@/actions/faq'
import { useQuery, useQueryClient } from '@tanstack/react-query'

export const ALL_CATEGORY_ID = 0

export const faqQueryKeys = {
  selectedCategory: ['faq', 'selected-category'] as const,
  categories: ['faq', 'categories'] as const,
  list: (categoryId: number) => ['faq', 'list', categoryId] as const,
}

export function useFaqSelectedCategory() {
  return useQuery<number>({
    queryKey: faqQueryKeys.selectedCategory,
    queryFn: () => ALL_CATEGORY_ID,
    staleTime: Infinity,
  })
}

export function useFaqCategories() {
  return useQuery({
    queryKey: faqQueryKeys.categories,
    queryFn: () => getFaqCategories(),
  })
}

export function useFaqList(categoryId: number) {
  return useQuery({
    queryKey: faqQueryKeys.list(categoryId),
    queryFn: () =>
      getFaqList(categoryId !== ALL_CATEGORY_ID ? { categoryId } : undefined),
  })
}

export function useSelectFaqCategory() {
  const queryClient = useQueryClient()
  return (categoryId: number) => queryClient.setQueryData(faqQueryKeys.selectedCategory, categoryId)
}
