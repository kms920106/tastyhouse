import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'

interface Props {
  request: string
}

export default function ReservationRequestAccordion({ request }: Props) {
  return (
    <Accordion type="single" collapsible defaultValue="reservation-request">
      <AccordionItem value="reservation-request" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">요청사항</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] py-2.5 pb-5">
            <p className="text-sm leading-relaxed whitespace-pre-wrap text-[#666666]">{request}</p>
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
