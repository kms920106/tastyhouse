import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import { PAYMENT_METHODS } from '@/domains/payment'
import type { PaymentMethod } from '@/domains/payment'
import { cn } from '@/lib/utils'

interface Props {
  selectedPaymentMethod: PaymentMethod | null
  onPaymentMethodSelect: (method: PaymentMethod) => void
}

export default function PaymentMethodSelector({
  selectedPaymentMethod,
  onPaymentMethodSelect,
}: Props) {
  const selectedMethod = PAYMENT_METHODS.find((method) => method.type === selectedPaymentMethod)

  return (
    <Accordion type="single" collapsible defaultValue="payment-method">
      <AccordionItem value="payment-method" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">결제방법 선택</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] pt-2.5 pb-5">
            <div className="grid grid-cols-2 gap-2.5">
              {PAYMENT_METHODS.map((method) => (
                <button
                  key={method.type}
                  onClick={() => onPaymentMethodSelect(method.type)}
                  className={cn(
                    'relative flex items-center justify-center py-[19px] text-sm leading-[14px] border box-border overflow-hidden',
                    selectedPaymentMethod === method.type ? 'border-main' : 'border-[#cccccc]',
                  )}
                >
                  {method.badge && (
                    <>
                      <div className="absolute -top-[24px] -left-[24px] w-[48px] h-[48px] bg-main rotate-45" />
                      <span className="absolute top-[6px] left-[3px] text-[8px] leading-[8px] text-white font-medium -rotate-45">
                        {method.badge}
                      </span>
                    </>
                  )}
                  <span>{method.label}</span>
                </button>
              ))}
            </div>
            {selectedMethod?.benefitTitle && selectedMethod?.benefitDescription && (
              <div className="mt-5 px-[16px] py-5 space-y-5 bg-[#f9f9f9] border border-line box-border">
                <p className="text-sm leading-[14px]">
                  <span className="font-bold">{selectedMethod.benefitTitle}</span>시 드리는 혜택
                </p>
                <p className="text-xs leading-[12px] text-[#666666]">
                  {selectedMethod.benefitDescription}
                </p>
              </div>
            )}
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
