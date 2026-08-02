export interface FaqCategory {
  id: number
  name: string
  sort: number
}

export interface Faq {
  id: number
  categoryId: number
  question: string
  answer: string
  sort: number
}
