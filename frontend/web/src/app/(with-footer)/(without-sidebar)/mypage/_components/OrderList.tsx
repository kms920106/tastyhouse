import EmptyState from '@/app/(with-footer)/(without-sidebar)/mypage/_components/EmptyState'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import type { Order } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import OrderListItem from './OrderListItem'

interface Props {
  orders: Order[]
  hasMoreOrders: boolean
}

export default function OrderList({ orders, hasMoreOrders }: Props) {
  if (orders.length === 0) {
    return (
      <>
        <EmptyState message="결제 내역이 없습니다." />
        <div className="h-[70px]" />
      </>
    )
  }

  return (
    <>
      <div className="px-[15px] py-[5px] bg-white divide-y divide-line">
        {orders.map((order) => (
          <OrderListItem
            key={order.id}
            id={order.id}
            shopThumbnailImageUrl={order.shopThumbnailImageUrl}
            shopName={order.shopName}
            firstProductName={order.firstProductName}
            totalItemCount={order.totalItemCount}
            price={order.amount}
            date={order.paymentDate}
            paymentStatus={order.paymentStatus}
          />
        ))}
      </div>
      {hasMoreOrders && (
        <div className="flex justify-center py-5">
          <ViewMoreButton href={PAGE_PATHS.ORDERS} label="더 보러가기" />
        </div>
      )}
      <div className="h-[70px]" />
    </>
  )
}
