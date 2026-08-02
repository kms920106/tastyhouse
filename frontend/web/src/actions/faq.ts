'use server'

import { faqRepository } from '@/domains/faq/faq.repository'

export async function getFaqCategories() {
  return faqRepository.getFaqCategories()
}

export async function getFaqList(params?: { categoryId?: number }) {
  return faqRepository.getFaqList(params)
}
