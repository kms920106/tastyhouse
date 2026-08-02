'use client'

import { getOrderList } from '@/actions/order'
import { useQuery } from '@tanstack/react-query'

const INITIAL_PAGE = 0
const PAGE_SIZE = 10

export const orderQueryKeys = {
  myOrders: ['mypage', 'orders'] as const,
}

export function useMyOrders() {
  return useQuery({
    queryKey: orderQueryKeys.myOrders,
    queryFn: async () => {
      const response = await getOrderList({ page: INITIAL_PAGE, size: PAGE_SIZE })
      return {
        orders: response.data || [],
        hasMoreOrders: (response.pagination?.totalElements ?? 0) > PAGE_SIZE,
      }
    },
  })
}
