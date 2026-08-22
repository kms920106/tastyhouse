import type { ProductOptionGroupType } from '../product'
import type { PaymentMethod, PaymentStatus } from '../payment'
import type { OrderUnavailableReasonCode } from '../shop'
import type { OrderMethodType } from './order.types'

export interface OrderMethod {
  code: OrderMethodType
  name: string
  /** 이 주문방식으로 지금 주문할 수 있는지 */
  orderable: boolean
  /** 화면에 표시하지 않고 사유별 분기에만 쓴다. orderable 이 true 면 null */
  unavailableReason: OrderUnavailableReasonCode | null
  /** 서버가 완성해 내려주는 한글 사유 문구 — 그대로 표시한다. orderable 이 true 면 null */
  unavailableReasonName: string | null
}

export interface OrderProductOption {
  groupId: number
  groupName: string
  optionId: number
  optionName: string
  additionalPrice: number
  /** 보증금 부과 대상 음료 개수. 보증금 옵션이 아니면 null(장바구니 표시용 — 서버 배치조회 응답 기반) */
  cupCount: number | null
  /** 보증금 금액. 보증금 옵션이 아니면 null(장바구니 표시용 — 서버 배치조회 응답 기반) */
  depositAmount: number | null
  /** 개인컵 사용 할인 금액. 개인컵 옵션이 아니면 null(장바구니 표시용 — 서버 배치조회 응답 기반) */
  personalCupDiscountAmount: number | null
}

export interface OrderProduct {
  productId: number
  optionKey: string
  name: string
  imageUrl: string
  quantity: number
  salePrice: number
  originalPrice: number
  discountPrice: number
  options: OrderProductOption[]
  available: boolean
  /**
   * 손님이 고른 가격 행 id. 가격이 1개인 메뉴는 `undefined` 다.
   *
   * 장바구니가 보관한 값을 그대로 나른다 — 주문 요청에 실어야 서버가 어느 가격으로 계산할지 안다.
   */
  priceId?: number
  /**
   * 가격명. 장바구니·주문서에서 메뉴 하위 항목으로 표시한다.
   *
   * 배치 조회 응답이 내려줄 때만 채워진다 — 없으면 표시하지 않는다(가격이 1개인 메뉴).
   */
  priceName?: string | null
}

export interface OrderedProductOption {
  id: number
  optionGroupName: string
  optionName: string
  additionalPrice: number
  /** 주문 시점 옵션그룹 유형 스냅샷 */
  groupType: ProductOptionGroupType
  /** 주문 시점 일회용컵 제공 개수 스냅샷. 보증금 옵션이 아니면 null */
  cupCount: number | null
  /** 주문 시점 보증금 금액 스냅샷. 보증금 옵션이 아니면 null */
  depositAmount: number | null
}

export interface OrderedProduct {
  orderProductId: number
  productId: number
  name: string
  /**
   * 주문 시점 가격명 스냅샷. 전표에서 메뉴 하위 항목으로 표시한다.
   *
   * 점주가 나중에 가격명을 바꿔도 이 값은 변하지 않는다(서버가 주문 시점 값을 박제한다).
   * 가격이 1개인 메뉴는 null 이므로 표시하지 않는다.
   */
  priceName: string | null
  imageUrl: string
  quantity: number
  originalPrice: number
  discountPrice: number | null
  totalOptionPrice: number
  totalPrice: number
  options: OrderedProductOption[]
  reviewed: boolean
}

export interface Order {
  id: number
  shopName: string
  shopThumbnailImageUrl: string
  firstProductName: string
  totalItemCount: number
  amount: number
  paymentStatus: PaymentStatus
  paymentDate: string
  /** 수령 예약 시각(슬롯 시작). null이면 즉시 주문 */
  scheduledAt: string | null
}

export interface OrderDetail {
  id: number
  orderNumber: string
  paymentStatus: PaymentStatus
  shopName: string
  shopPhoneNumber: string
  ordererName: string
  ordererPhone: string
  ordererEmail: string
  totalProductAmount: number
  productDiscountAmount: number
  couponDiscountAmount: number
  pointDiscountAmount: number
  totalDiscountAmount: number
  finalAmount: number
  /** 일회용컵 보증금 합계. 컵 반납 시 환급되며 할인이 아니다 */
  cupDepositAmount: number
  usedPoint: number
  earnedPoint: number
  orderProducts: OrderedProduct[]
  payment: OrderPayment
  createdAt: string
  /** 수령 예약 시각(슬롯 시작). null이면 즉시 주문 */
  scheduledAt: string | null
  /** 수령 예약 슬롯 종료 시각. 포장은 scheduledAt과 동일 */
  scheduledSlotEndAt: string | null
}

export interface OrderPayment {
  id: number
  approvedAt: string
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  cardCompany: string | null
  cardNumber: string | null
}
