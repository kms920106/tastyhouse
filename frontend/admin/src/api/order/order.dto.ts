// 주문 상태: PENDING(대기) / CONFIRMED(확정) / PREPARING(준비중) / COMPLETED(완료) / CANCELLED(취소)
export type OrderStatus = "PENDING" | "CONFIRMED" | "PREPARING" | "COMPLETED" | "CANCELLED";

// 주문 방식: TABLE(테이블 오더) / RESERVATION(예약) / DELIVERY(배달) / TAKEOUT(포장)
export type OrderMethod = "TABLE" | "RESERVATION" | "DELIVERY" | "TAKEOUT";

// 결제 상태: PENDING(결제 대기) / COMPLETED(결제 완료) / FAILED(결제 실패) / CANCELLED(결제 취소)
export type PaymentStatus = "PENDING" | "COMPLETED" | "FAILED" | "CANCELLED";

// 주문 목록 조회
export interface OrderListQueryRequest {
  shopId?: number;
  orderStatus?: OrderStatus;
  orderMethod?: OrderMethod;
  paymentStatus?: PaymentStatus;
  orderNumber?: string;
  ordererName?: string;
  startDate?: string;
  endDate?: string;
}

// 주문 목록 조회
export interface OrderListItemResponse {
  id: number;
  orderNumber: string;
  shopName: string;
  ordererName: string;
  orderMethod: OrderMethod;
  orderStatus: OrderStatus;
  paymentStatus: PaymentStatus | null;
  finalAmount: number;
  totalItemCount: number;
  createdAt: string;
}

// 주문 상품 선택 옵션
export interface OrderProductOptionResponse {
  groupId: number;
  groupName: string;
  optionId: number;
  optionName: string;
  additionalPrice: number;
}

// 주문 상품
export interface OrderProductResponse {
  id: number;
  productId: number;
  name: string;
  imageUrl: string;
  quantity: number;
  originalPrice: number;
  discountPrice: number | null;
  totalOptionPrice: number;
  totalPrice: number;
  selectedOptions: OrderProductOptionResponse[];
}

// 결제 요약 정보
export interface PaymentSummaryResponse {
  id: number;
  paymentMethod: string;
  paymentStatus: PaymentStatus;
  amount: number;
  cardCompany: string | null;
  cardNumber: string | null;
  approvedAt: string | null;
  receiptUrl: string | null;
}

// 주문 상세 조회
export interface OrderDetailResponse {
  id: number;
  orderNumber: string;
  orderMethod: OrderMethod;
  paymentStatus: PaymentStatus | null;
  shopName: string;
  shopPhoneNumber: string;
  ordererName: string;
  ordererPhone: string;
  ordererEmail: string | null;
  totalProductAmount: number;
  productDiscountAmount: number;
  couponDiscountAmount: number;
  pointDiscountAmount: number;
  totalDiscountAmount: number;
  finalAmount: number;
  usedPoint: number;
  earnedPoint: number;
  orderProducts: OrderProductResponse[];
  payment: PaymentSummaryResponse | null;
  approvedAt: string | null;
  createdAt: string;
}

// 주문 상태 변경
export interface OrderStatusUpdateRequest {
  status: OrderStatus;
}
