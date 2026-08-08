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

/**
 * 수령 예약시간 표시: "2026. 08. 08 18:00". 즉시 주문이면 "-".
 *
 * PDF 규칙에 따라 슬롯 **시작 시각만** 표기한다. 배달은 30분 범위 슬롯이지만 점주에게는
 * 범위가 아니라 시작 시각만 보여준다(18:00~18:30 → "18:00"). 그래서 API가 함께 내려주는
 * `scheduledSlotEndAt`은 화면에서 쓰지 않는다.
 */
export function formatScheduledAt(scheduledAt: string | null): string {
  if (scheduledAt == null) return "-";

  const parsed = new Date(scheduledAt);
  if (Number.isNaN(parsed.getTime())) return "-";

  const date = parsed.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
  const time = parsed.toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });

  return `${date} ${time}`;
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
