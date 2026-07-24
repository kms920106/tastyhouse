/** 가격 표시: "5,000원" (미지정=대시) */
export function formatPrice(value: number | null | undefined): string {
  if (value == null) return "-";
  return `${value.toLocaleString("ko-KR")}원`;
}

/** 할인율 표시: "10%" (미지정=대시) */
export function formatDiscountRate(value: number | null | undefined): string {
  if (value == null) return "-";
  return `${value}%`;
}

/** 맵기 단계 표시 (미지정=대시, 0=없음, 1~5=🌶 개수) */
export function formatSpiciness(value: number | null | undefined): string {
  if (value == null) return "-";
  if (value <= 0) return "없음";
  return `${value}단계`;
}
