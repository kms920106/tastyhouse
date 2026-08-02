import { toTimestamp } from '@/lib/date'
import type { ReviewSortType } from '@/domains/review'

export interface SortableReview {
  createdAt: string
}

export function sortReviews<T extends SortableReview>(reviews: T[], sortType: ReviewSortType): T[] {
  const sorted = [...reviews]
  switch (sortType) {
    case 'recommended':
      return sorted
    case 'latest':
      return sorted.sort((a, b) => toTimestamp(b.createdAt) - toTimestamp(a.createdAt))
    case 'oldest':
      return sorted.sort((a, b) => toTimestamp(a.createdAt) - toTimestamp(b.createdAt))
    default:
      return sorted
  }
}
