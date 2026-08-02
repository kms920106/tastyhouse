import type { DiscountType } from "./domain";

/** 할인 값 표시: AMOUNT 는 "5,000원", RATE 는 "10%" */
export function formatDiscountValue(discountType: DiscountType, discountAmount: number): string {
  if (discountType === "RATE") {
    return `${discountAmount}%`;
  }
  return `${discountAmount.toLocaleString("ko-KR")}원`;
}

/** 할인 유형 한글 라벨 */
export function discountTypeLabel(discountType: DiscountType): string {
  return discountType === "RATE" ? "정률" : "정액";
}

/** 최대 할인 금액 표시 (미지정=무제한) */
export function formatMaxDiscountAmount(value: number | null | undefined): string {
  if (value == null) return "무제한";
  return `${value.toLocaleString("ko-KR")}원`;
}

/** 최대 발급 수량 표시 (미지정=무제한) */
export function formatMaxDiscountCount(value: number | null | undefined): string {
  if (value == null) return "무제한";
  return `${value.toLocaleString("ko-KR")}건`;
}
