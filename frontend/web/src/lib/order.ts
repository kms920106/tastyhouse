import dayjs from '@/lib/dayjs'

/**
 * 수령 예약 시각을 주문 조회 화면 배지 문구로 포맷팅합니다.
 *
 * 예약 슬롯 선택 화면과 달리 주문 조회 API는 완성된 표시 문구를 내려주지 않고 시각만 주므로,
 * 여기서 포맷합니다. PDF 규칙에 따라 슬롯 **시작 시각만** 표기하고 종료 시각은 쓰지 않습니다.
 *
 * @param scheduledAt 수령 예약 시각. null이면 즉시 주문이므로 null을 돌려준다
 * @returns 예: `"10. 14 (화) 오후 6:00 수령 예정"`. 즉시 주문이면 null
 */
export function formatScheduledPickupLabel(scheduledAt: string | null): string | null {
  if (!scheduledAt) return null

  const parsed = dayjs(scheduledAt)
  if (!parsed.isValid()) return null

  return `${parsed.format('M. D (ddd) A h:mm')} 수령 예정`
}

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
