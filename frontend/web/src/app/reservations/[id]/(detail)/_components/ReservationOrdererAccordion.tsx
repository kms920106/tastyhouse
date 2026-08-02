import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import { formatPhoneNumber } from '@/lib/utils'

interface Props {
  ordererName: string
  ordererPhone: string | null
  ordererEmail: string
}

export default function ReservationOrdererAccordion({
  ordererName,
  ordererPhone,
  ordererEmail,
}: Props) {
  return (
    <Accordion type="single" collapsible defaultValue="orderer-info">
      <AccordionItem value="orderer-info" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">예약자 정보</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">예약하는 분</span>
                <span className="text-sm leading-[14px]">{ordererName}</span>
              </div>
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">휴대폰</span>
                <span className="text-sm leading-[14px]">
                  {ordererPhone ? formatPhoneNumber(ordererPhone) : '-'}
                </span>
              </div>
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">이메일</span>
                <span className="text-sm leading-[14px]">{ordererEmail}</span>
              </div>
            </div>
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
