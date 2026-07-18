/** 평점 표시: "4.5" (미지정=대시) */
export function formatRating(value: number | null | undefined): string {
  if (value == null) return "-";
  return String(value);
}

/** 재방문 의사 표시 */
export function formatWillRevisit(value: boolean): string {
  return value ? "재방문 의사 있음" : "재방문 의사 없음";
}
