import 'server-only'

import { api } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import type { OrderCreateRequest, OrderListItemResponse } from './order.dto'
import type { OrderDetail } from './order.model'

const ENDPOINT = '/api/orders'

export const orderRepository = {
  // 주문 생성. 서버는 생성된 주문 ID를 스칼라로 반환한다(ApiResponse<Long>).
  async createOrder(request: OrderCreateRequest) {
    return api.post<number>(`${ENDPOINT}/v1`, request)
  },
  // 주문 목록 조회
  async getOrderList(params: PaginationParams) {
    return api.get<OrderListItemResponse[]>(`${ENDPOINT}/v1`, { params })
  },
  // 주문 상세 조회
  async getOrderDetail(orderId: number) {
    return api.get<OrderDetail>(`${ENDPOINT}/v1/${orderId}`)
  },
}
