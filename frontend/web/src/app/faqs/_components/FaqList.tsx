'use client'

import { Accordion } from '@/components/ui/shadcn/accordion'
import { ALL_CATEGORY_ID, useFaqCategories, useFaqList, useFaqSelectedCategory } from '@/domains/faq/faq.hook'
import FaqAccordionItem from './FaqAccordionItem'
import FaqListItemSkeleton from './FaqListItemSkeleton'

export default function FaqList() {
  const { data: selectedCategoryId = ALL_CATEGORY_ID } = useFaqSelectedCategory()
  const { data: categoriesData } = useFaqCategories()
  const { data, isFetching } = useFaqList(selectedCategoryId)

  const faqs = data?.data ?? []
  const categories = categoriesData?.data ?? []

  const getCategoryName = (categoryId: number) =>
    categories.find((c) => c.id === categoryId)?.name ?? ''

  if (isFetching) {
    return <FaqListItemSkeleton />
  }

  return (
    <Accordion type="multiple" className="w-full">
      {faqs.map((faq) => (
        <FaqAccordionItem key={faq.id} faq={faq} categoryName={getCategoryName(faq.categoryId)} />
      ))}
    </Accordion>
  )
}
