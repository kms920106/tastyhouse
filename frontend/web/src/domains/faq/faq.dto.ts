export interface FaqListQuery {
  categoryId?: number
}

export interface FaqCategoryListItemResponse {
  id: number
  name: string
  sort: number
}

export interface FaqListItemResponse {
  id: number
  categoryId: number
  question: string
  answer: string
  sort: number
}
