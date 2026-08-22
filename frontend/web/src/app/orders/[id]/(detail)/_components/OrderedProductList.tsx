import OrderProductItem from '@/components/orders/OrderProductItem'
import { MENU_REVIEW_COPY } from '@/domains/menu-review'
import type { OrderedProduct } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  orderId: number
  shopName: string
  orderProducts: OrderedProduct[]
}

export default function OrderedProductList({ orderId, shopName, orderProducts }: Props) {
  return (
    <>
      <div className="flex items-center justify-between gap-2 px-[15px] pt-5 pb-[15px]">
        <h2 className="text-base leading-[16px]">{shopName}</h2>
        {/* 매장 리뷰를 쓰지 않고도 메뉴 평가만 남길 수 있는 단독 진입점 */}
        <Link
          href={PAGE_PATHS.ORDER_MENU_REVIEWS(orderId)}
          className="flex items-center justify-center shrink-0 px-[11px] py-2.5 text-xs leading-[12px] text-main border border-main box-border"
        >
          {MENU_REVIEW_COPY.SECTION_TITLE}
        </Link>
      </div>
      <div className="px-4 pb-[5px]">
        <div className="divide-y divide-line first:border-t border-line">
          {orderProducts.map((orderProduct) => (
            <OrderProductItem
              key={orderProduct.orderProductId}
              productName={orderProduct.name}
              priceName={orderProduct.priceName}
              productImageUrl={orderProduct.imageUrl}
              totalPrice={orderProduct.totalPrice}
              quantity={orderProduct.quantity}
              options={orderProduct.options}
              action={
                orderProduct.reviewed ? (
                  <Link
                    href={PAGE_PATHS.ORDERS_REVIEWS_EDIT(orderProduct.orderProductId)}
                    className="flex items-center justify-center shrink-0 px-[11px] py-2.5 text-xs leading-[12px] text-main border border-main box-border"
                  >
                    리뷰수정
                  </Link>
                ) : (
                  <Link
                    href={PAGE_PATHS.ORDERS_REVIEWS_CREATE(orderProduct.orderProductId)}
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
