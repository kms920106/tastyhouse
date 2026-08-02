'use client'

import type { ReviewListItemData } from '@/components/reviews/ReviewListItem'
import type { ReviewSortType } from '@/domains/review'
import { sortReviews } from '@/lib/review'
import { useState } from 'react'

export interface ReviewPanelData {
  reviewsByRating: Record<string, ReviewListItemData[]>
  allReviews: ReviewListItemData[]
  totalReviewCount: number
}

export function useReviewPanel() {
  const [photoOnly, setPhotoOnly] = useState(false)
  const [selectedRating, setSelectedRating] = useState<number | null>(null)
  const [sortType, setSortType] = useState<ReviewSortType>('latest')

  function deriveReviews(data: ReviewPanelData) {
    let filtered =
      selectedRating !== null ? data.reviewsByRating[String(selectedRating)] || [] : data.allReviews

    if (photoOnly) {
      filtered = filtered.filter((r) => r.imageUrls.length > 0)
    }

    return {
      sortedReviews: sortReviews(filtered, sortType),
      photoReviewCount: data.allReviews.filter((r) => r.imageUrls.length > 0).length,
    }
  }

  return {
    photoOnly,
    setPhotoOnly,
    selectedRating,
    setSelectedRating,
    sortType,
    setSortType,
    deriveReviews,
  }
}
