import OrderProductItem from '@/components/orders/OrderProductItem'
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/shadcn/accordion'
import { formatOrderSummary } from '@/lib/order'

interface OrderProduct {
  name: string
  imageUrl: string
  salePrice: number
  quantity: number
}

interface Props {
  shopName: string
  orderProducts: OrderProduct[]
  firstProductName: string
  totalItemCount: number
}

export default function OrderInfoSection({
  shopName,
  orderProducts: items,
  firstProductName,
  totalItemCount,
}: Props) {
  return (
    <Accordion type="single" collapsible defaultValue="order-info">
      <AccordionItem value="order-info" className="border-b-0">
        <AccordionTrigger className="items-center px-[15px] pt-5 pb-[15px] hover:no-underline">
          <div className="flex-1 flex items-center justify-between gap-2">
            <h2 className="text-base leading-[16px]">{shopName}</h2>
            <span className="text-xs leading-[12px] text-[#aaaaaa]">
              {formatOrderSummary(firstProductName, totalItemCount)}
            </span>
          </div>
        </AccordionTrigger>
        <AccordionContent className="p-0">
          <div className="px-4">
            <div className="divide-y divide-line first:border-t border-line">
              {items.map((orderProduct, index) => (
                <OrderProductItem
                  key={index}
                  productName={orderProduct.name}
                  productImageUrl={orderProduct.imageUrl}
                  totalPrice={orderProduct.salePrice}
                  quantity={orderProduct.quantity}
                />
              ))}
            </div>
          </div>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  )
}
