import { formatDate } from '@/lib/date'
import { cn } from '@/lib/utils'
import { AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/shadcn/accordion'

export interface Notice {
  id: number
  title: string
  content: string
  createdAt: string
}

interface NoticeListItemProps {
  notice: Notice
  isFirst?: boolean
}

export function NoticeListItem({ notice, isFirst }: NoticeListItemProps) {
  const { id, title, content, createdAt } = notice

  return (
    <AccordionItem value={String(id)} className="border-line">
      <AccordionTrigger
        className={cn('w-full px-[16px] py-[18px] hover:no-underline', isFirst && 'pt-0')}
        showIcon={false}
      >
        <div className="flex flex-col items-start gap-3">
          <span className="text-sm leading-[14px]">{title}</span>
          <span className="text-[13px] leading-[13px] text-[#999999] font-thin">
            {formatDate(createdAt, 'YYYY-MM-DD')}
          </span>
        </div>
      </AccordionTrigger>
      <AccordionContent className="px-[25px] py-[25px] text-[13px] leading-[22px] bg-[#f9f9f9] whitespace-pre-line">
        {content}
      </AccordionContent>
    </AccordionItem>
  )
}
