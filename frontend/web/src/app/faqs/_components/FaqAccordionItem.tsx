'use client'

import { AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/shadcn/accordion'
import { Faq } from '@/domains/faq'

interface Props {
  faq: Faq
  categoryName: string
}

export default function FaqAccordionItem({ faq, categoryName }: Props) {
  return (
    <AccordionItem value={String(faq.id)} className="border-line">
      <AccordionTrigger
        className="w-full px-[19px] py-[18px] hover:no-underline cursor-pointer"
        showIcon={false}
      >
        <div className="flex items-center gap-5">
          <span className="text-sm leading-[14px] text-[#a11c20] flex-shrink-0">
            {categoryName}
          </span>
          <h2 className="text-sm leading-[14px]">{faq.question}</h2>
        </div>
      </AccordionTrigger>
      <AccordionContent className="px-[26px] py-6 text-[13px] leading-[13px] bg-[#f9f9f9] whitespace-pre-line">
        {faq.answer}
      </AccordionContent>
    </AccordionItem>
  )
}
