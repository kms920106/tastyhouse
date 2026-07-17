import type { OrderMethod, OrderStatus, PaymentStatus } from "./domain";

// 주문 상태 옵션 (Select/필터 공용)
export const ORDER_STATUS_OPTIONS: { value: OrderStatus; label: string }[] = [
  { value: "PENDING", label: "대기" },
  { value: "CONFIRMED", label: "확정" },
  { value: "PREPARING", label: "준비중" },
  { value: "COMPLETED", label: "완료" },
  { value: "CANCELLED", label: "취소" },
];

// 주문 방식 옵션 (Select/필터 공용)
export const ORDER_METHOD_OPTIONS: { value: OrderMethod; label: string }[] = [
  { value: "TABLE", label: "테이블 오더" },
  { value: "RESERVATION", label: "예약" },
  { value: "DELIVERY", label: "배달" },
  { value: "TAKEOUT", label: "포장" },
];

// 결제 상태 옵션 (Select/필터 공용)
export const PAYMENT_STATUS_OPTIONS: { value: PaymentStatus; label: string }[] = [
  { value: "PENDING", label: "결제 대기" },
  { value: "COMPLETED", label: "결제 완료" },
  { value: "FAILED", label: "결제 실패" },
  { value: "CANCELLED", label: "결제 취소" },
];
