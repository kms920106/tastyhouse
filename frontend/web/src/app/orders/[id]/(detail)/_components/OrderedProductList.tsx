import OrderProductItem from '@/components/orders/OrderProductItem'
import type { OrderedProduct } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopName: string
  orderProducts: OrderedProduct[]
}

export default function OrderedProductList({ shopName, orderProducts }: Props) {
  return (
    <>
      <div className="px-[15px] pt-5 pb-[15px]">
        <h2 className="text-base leading-[16px]">{shopName}</h2>
      </div>
      <div className="px-4 pb-[5px]">
        <div className="divide-y divide-line first:border-t border-line">
          {orderProducts.map((orderProduct) => (
            <OrderProductItem
              key={orderProduct.id}
              productName={orderProduct.name}
              productImageUrl={orderProduct.imageUrl}
              totalPrice={orderProduct.totalPrice}
              quantity={orderProduct.quantity}
              options={orderProduct.options}
              action={
                orderProduct.reviewed ? (
                  <Link
                    href={PAGE_PATHS.ORDERS_REVIEWS_EDIT(orderProduct.id)}
                    className="flex items-center justify-center shrink-0 px-[11px] py-2.5 text-xs leading-[12px] text-main border border-main box-border"
                  >
                    리뷰수정
                  </Link>
                ) : (
                  <Link
                    href={PAGE_PATHS.ORDERS_REVIEWS_CREATE(orderProduct.id)}
                    className="flex items-center justify-center shrink-0 px-[11px] py-2.5 bg-main text-xs leading-[12px] text-white"
                  >
                    리뷰작성
                  </Link>
                )
              }
            />
          ))}
        </div>
      </div>
    </>
  )
}
