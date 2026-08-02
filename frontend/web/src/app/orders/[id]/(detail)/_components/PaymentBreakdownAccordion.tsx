import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import { formatNumber } from '@/lib/number'

interface Props {
  totalProductAmount: number
  productDiscountAmount: number
  couponDiscountAmount: number
  pointDiscountAmount: number
  totalDiscountAmount: number
  finalAmount: number
}

export default function PaymentBreakdownAccordion({
  totalProductAmount,
  productDiscountAmount,
  couponDiscountAmount,
  pointDiscountAmount,
  totalDiscountAmount,
  finalAmount,
}: Props) {
  return (
    <Accordion type="single" collapsible defaultValue="payment-breakdown">
      <AccordionItem value="payment-breakdown" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] py-5 hover:no-underline">
          <h2 className="text-base leading-[16px]">결제 내역</h2>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              <div className="flex justify-between">
                <span className="text-sm leading-[14px]">상품금액</span>
                <span className="text-sm leading-[14px]">
                  {formatNumber(totalProductAmount)}원
                </span>
              </div>
              <div>
                <div className="flex justify-between">
                  <span className="text-sm leading-[14px]">할인금액</span>
                  <span className="text-sm leading-[14px]">
                    {totalDiscountAmount > 0
                      ? `- ${formatNumber(totalDiscountAmount)}원`
                      : '0원'}
                  </span>
                </div>
                {totalDiscountAmount > 0 && (
                  <div className="pt-2.5 space-y-2.5">
                    {productDiscountAmount > 0 && (
                      <div className="flex justify-between">
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">상품 할인</span>
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">
                          - {formatNumber(productDiscountAmount)}원
                        </span>
                      </div>
                    )}
                    {couponDiscountAmount > 0 && (
                      <div className="flex justify-between">
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">쿠폰 사용</span>
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">
                          - {formatNumber(couponDiscountAmount)}원
                        </span>
                      </div>
                    )}
                    {pointDiscountAmount > 0 && (
                      <div className="flex justify-between">
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">포인트 사용</span>
                        <span className="text-xs leading-[12px] text-[#aaaaaa]">
                          - {formatNumber(pointDiscountAmount)}원
                        </span>
                      </div>
                    )}
                  </div>
                )}
              </div>
              <div className="flex justify-between">
                <span className="text-sm leading-[14px]">최종 결제금액</span>
                <span className="text-sm leading-[14px]">{formatNumber(finalAmount)}원</span>
              </div>
            </div>
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
