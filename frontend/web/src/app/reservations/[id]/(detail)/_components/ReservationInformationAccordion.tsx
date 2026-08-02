import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'

interface Props {
  reservationDateText: string
  reservationTimeText: string
  partySize: number
  createdAtText: string
}

export default function ReservationInformationAccordion({
  reservationDateText,
  reservationTimeText,
  partySize,
  createdAtText,
}: Props) {
  return (
    <Accordion type="single" collapsible defaultValue="reservation-info">
      <AccordionItem value="reservation-info" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">예약 정보</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">예약 날짜</span>
                <span className="text-sm leading-[14px]">{reservationDateText}</span>
              </div>
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">예약 시간</span>
                <span className="text-sm leading-[14px]">{reservationTimeText}</span>
              </div>
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">인원</span>
                <span className="text-sm leading-[14px]">{partySize}명</span>
              </div>
              <div className="flex">
                <span className="w-30 text-sm leading-[14px] text-[#666666]">예약 신청일</span>
                <span className="text-sm leading-[14px]">{createdAtText}</span>
              </div>
            </div>
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
