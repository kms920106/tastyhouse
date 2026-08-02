/**
 * 주문 상품 요약 텍스트를 포맷팅합니다.
 * @param firstProductName - 첫 번째 상품명
 * @param totalItemCount - 전체 상품 개수
 * @returns 포맷팅된 주문 요약 텍스트 (예: "아메리카노 1건" 또는 "아메리카노 외 2건")
 */
export function formatOrderSummary(firstProductName: string, totalItemCount: number): string {
  const suffix = totalItemCount > 1 ? ` 외 ${totalItemCount - 1}건` : ''
  return `${firstProductName}${suffix}`
}
