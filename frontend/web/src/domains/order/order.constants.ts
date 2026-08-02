import type { OrderMethodType, OrderStatusCode } from './order.types'

const ORDER_STATUS_NAMES: Record<OrderStatusCode, string> = {
  PENDING: '주문대기',
  CONFIRMED: '주문확인',
  PREPARING: '준비 중',
  COMPLETED: '주문완료',
  CANCELLED: '주문취소',
}

export const getOrderStatusName = (status: OrderStatusCode): string => {
  return ORDER_STATUS_NAMES[status]
}

const ORDER_METHOD_TYPES: OrderMethodType[] = ['TABLE', 'RESERVATION', 'DELIVERY', 'TAKEOUT']

export const parseOrderMethodType = (value: string | undefined): OrderMethodType | null => {
  return ORDER_METHOD_TYPES.includes(value as OrderMethodType) ? (value as OrderMethodType) : null
}

// URL 경로 세그먼트는 소문자 kebab-case로 노출한다 (예: TABLE ↔ table-order).
// 도메인 타입(대문자 언더스코어)과 URL 표기 사이 변환을 이 경계 함수 한 쌍으로 일원화한다.
const ORDER_METHOD_SLUGS: Record<OrderMethodType, string> = {
  TABLE: 'table',
  RESERVATION: 'reservation',
  DELIVERY: 'delivery',
  TAKEOUT: 'takeout',
}

const ORDER_METHOD_SLUG_TO_TYPE: Record<string, OrderMethodType> = Object.fromEntries(
  Object.entries(ORDER_METHOD_SLUGS).map(([type, slug]) => [slug, type as OrderMethodType]),
)

// 도메인 타입 → URL 세그먼트 (경로 생성용)
export const toOrderMethodSlug = (method: OrderMethodType): string => ORDER_METHOD_SLUGS[method]

// URL 세그먼트 → 도메인 타입 (경로 파싱용, 유효하지 않으면 null)
export const parseOrderMethodSlug = (value: string | undefined): OrderMethodType | null => {
  return value !== undefined && value in ORDER_METHOD_SLUG_TO_TYPE
    ? ORDER_METHOD_SLUG_TO_TYPE[value]
    : null
}
