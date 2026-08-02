'use client'

import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMyOrders } from '@/domains/order/order.hook'
import OrderList from './OrderList'
import { OrderListSkeleton } from './OrderListSkeleton'

export default function MyPageOrderTabContent() {
  const { data, isLoading, error } = useMyOrders()

  if (isLoading) {
    return <OrderListSkeleton />
  }

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('주문 내역')} />
  }

  return <OrderList orders={data.orders} hasMoreOrders={data.hasMoreOrders} />
}
