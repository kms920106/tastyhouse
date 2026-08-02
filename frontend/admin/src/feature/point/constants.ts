import type { PointType } from "./domain";

// 포인트 유형 옵션 (Select/필터 공용)
export const POINT_TYPE_OPTIONS: { value: PointType; label: string }[] = [
  { value: "EARNED", label: "적립" },
  { value: "USE", label: "사용" },
  { value: "REFUND", label: "환불" },
];

// 포인트 사유 최대 길이
export const REASON_MAX = 200;
