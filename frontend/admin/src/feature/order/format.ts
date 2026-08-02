import type { OrderMethod, OrderStatus, PaymentStatus } from "./domain";

// 주문 상태 한글 라벨
export function orderStatusLabel(status: OrderStatus): string {
  switch (status) {
    case "PENDING":
      return "대기";
    case "CONFIRMED":
      return "확정";
    case "PREPARING":
      return "준비중";
    case "COMPLETED":
      return "완료";
    case "CANCELLED":
      return "취소";
    default:
      return status;
  }
}

// 주문 상태 Badge variant
export function orderStatusBadgeVariant(status: OrderStatus): "default" | "secondary" | "outline" | "destructive" {
  switch (status) {
    case "COMPLETED":
      return "default";
    case "CANCELLED":
      return "destructive";
    case "PENDING":
      return "outline";
    default:
      return "secondary";
  }
}

// 주문 방식 한글 라벨
export function orderMethodLabel(method: OrderMethod): string {
  switch (method) {
    case "TABLE":
      return "테이블 오더";
    case "RESERVATION":
      return "예약";
    case "DELIVERY":
      return "배달";
    case "TAKEOUT":
      return "포장";
    default:
      return method;
  }
}

// 결제 상태 한글 라벨 (결제 정보 없으면 "-")
export function paymentStatusLabel(status: PaymentStatus | null): string {
  if (status == null) return "-";
  switch (status) {
    case "PENDING":
      return "결제 대기";
    case "COMPLETED":
      return "결제 완료";
    case "FAILED":
      return "결제 실패";
    case "CANCELLED":
      return "결제 취소";
    default:
      return status;
  }
}

// 결제 상태 Badge variant
export function paymentStatusBadgeVariant(
  status: PaymentStatus | null,
): "default" | "secondary" | "outline" | "destructive" {
  if (status == null) return "outline";
  switch (status) {
    case "COMPLETED":
      return "default";
    case "FAILED":
    case "CANCELLED":
      return "destructive";
    default:
      return "secondary";
  }
}

/** 금액 표시: "21,000원" */
export function formatWon(amount: number | null | undefined): string {
  if (amount == null) return "-";
  return `${amount.toLocaleString("ko-KR")}원`;
}

/** 포인트 표시: "1,400P" */
export function formatPoint(amount: number | null | undefined): string {
  if (amount == null) return "-";
  return `${amount.toLocaleString("ko-KR")}P`;
}
