import type { OrderErrorCode, OrderMethodType, OrderStatusCode } from './order.types'

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

// 주문 생성 에러 코드 → 사용자 노출 메시지 (백엔드 message가 없을 때의 폴백)
const ORDER_ERROR_MESSAGES: Record<OrderErrorCode, string> = {
  SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET: '가게 최소주문금액을 충족하지 않습니다.',
  ORDER_MINIMUM_AMOUNT_NOT_MET: '쿠폰 최소주문금액을 충족하지 않습니다.',
  ORDER_PRODUCT_SOLD_OUT: '품절된 상품이 포함되어 있습니다. 장바구니를 확인해 주세요.',
  ORDER_PRODUCT_NOT_FOUND: '판매하지 않는 상품이 포함되어 있습니다. 장바구니를 확인해 주세요.',
  SHOP_NOT_FOUND: '존재하지 않는 가게입니다.',
  ORDER_DELIVERY_TIP_AMOUNT_MISMATCH: '배달팁이 변경되었습니다. 결제 금액을 다시 확인해 주세요.',
  // 대개 점주가 그 사이 가격을 바꾼 상황이다. 재시도가 아니라 재조회를 안내한다.
  ORDER_PRODUCT_AMOUNT_MISMATCH: '가격 정보가 변경되었습니다. 새로고침 후 다시 시도해 주세요.',
  ORDER_DELIVERY_ADDRESS_REQUIRED: '배달 주소를 입력해 주세요.',
  ORDER_DELIVERY_AREA_NOT_COVERED: '선택하신 주소는 이 가게의 배달 가능 지역이 아닙니다.',
  SHOP_SCHEDULED_ORDER_DISABLED: '이 가게는 예약주문을 받고 있지 않아요',
  ORDER_SCHEDULE_METHOD_NOT_SUPPORTED: '이 주문방식은 예약할 수 없어요',
  ORDER_SCHEDULED_AT_UNAVAILABLE: '선택한 수령시간이 마감되었어요. 다시 선택해주세요',
  SHOP_NOT_ORDERABLE: '현재 주문할 수 없는 가게입니다.',
  SHOP_ORDER_METHOD_NOT_SUPPORTED: '이 가게가 지원하지 않는 주문방식입니다.',
  SHOP_ORDER_METHOD_SUSPENDED: '해당 주문방식은 현재 일시적으로 중단되었습니다.',
}

/**
 * 주문방식 선택 화면 안내 문구.
 *
 * 개별 주문방식의 불가 사유는 서버가 `unavailableReasonName` 으로 내려주므로 프론트에 매핑을 두지 않는다.
 */
export const ORDER_METHOD_COPY = {
  SELECT_TITLE: '원하시는 주문방법을 선택해 주세요.',
  SELECT_DESCRIPTION_LINE1: '가게 사정에 따라 가능한 주문방법이 달라질 수 있으며,',
  SELECT_DESCRIPTION_LINE2: '자세한 사항은 가게 정보를 확인해 주세요.',
  NEXT_BUTTON: '다음',
  // 배정된 주문방식이 있으나 전부 불가 — 일시적 상태이므로 재확인을 안내한다
  ALL_UNAVAILABLE_TITLE: '지금은 주문할 수 없어요',
  ALL_UNAVAILABLE_DESCRIPTION:
    '가게 사정에 따라 주문이 일시적으로 중단되었습니다. 잠시 후 다시 확인해 주세요.',
  // 배정된 주문방식이 0건 — 기다려도 해결되지 않으므로 위와 다른 문구를 쓴다
  NONE_ASSIGNED_TITLE: '주문을 받지 않는 가게예요',
  NONE_ASSIGNED_DESCRIPTION: '이 가게는 온라인 주문을 운영하지 않습니다.',
} as const

export const getOrderErrorMessage = (errorCode: string | undefined): string | null => {
  if (!errorCode) return null
  return ORDER_ERROR_MESSAGES[errorCode as OrderErrorCode] ?? null
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
