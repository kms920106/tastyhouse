import type { PointType } from "./domain";

// 포인트 유형 한글 라벨
export function pointTypeLabel(type: PointType): string {
  switch (type) {
    case "EARNED":
      return "적립";
    case "USE":
      return "사용";
    case "REFUND":
      return "환불";
    default:
      return type;
  }
}

// 포인트 유형 Badge variant
export function pointTypeBadgeVariant(type: PointType): "default" | "secondary" | "outline" {
  switch (type) {
    case "EARNED":
      return "default";
    case "REFUND":
      return "outline";
    default:
      return "secondary";
  }
}

/** 증감량을 부호와 함께 천 단위 구분 문자열로 변환 (예: +500, -1,000) */
export function formatSignedPoint(amount: number): string {
  const sign = amount < 0 ? "-" : "+";
  return `${sign}${Math.abs(amount).toLocaleString()}`;
}
