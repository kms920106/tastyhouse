import { SHOP_REVIEW_COPY } from "./message";

/**
 * 별점 표시 포맷 — UI 무관한 순수 함수라 컴포넌트에 인라인하지 않는다
 * (`feature/shop/time.ts`·`geo.ts` 와 같은 성격).
 */

/** 별점을 소수 1자리로 표시한다. 미지정은 대시 */
export function formatRating(value: number | null | undefined): string {
  if (value == null) return SHOP_REVIEW_COPY.VALUE_ABSENT;
  return value.toFixed(1);
}

/** 리뷰 수·건수 등 정수 표시. 미지정은 대시 */
export function formatCount(value: number | null | undefined): string {
  if (value == null) return SHOP_REVIEW_COPY.VALUE_ABSENT;
  return value.toLocaleString("ko-KR");
}

/** 비율(%) 표시. 소수점은 버려 정수로 보여준다 */
export function formatPercentage(value: number | null | undefined): string {
  if (value == null) return SHOP_REVIEW_COPY.VALUE_ABSENT;
  return `${Math.round(value)}%`;
}

/**
 * 16자리 리뷰번호를 4자리씩 끊어 읽기 쉽게 만든다.
 *
 * 서버가 이미 0-pad 된 문자열을 내려주므로 자리수를 다시 맞추지 않고,
 * 길이가 예상과 다르면 원문을 그대로 보여준다 — 표시 포맷 때문에 값이 잘리면 안 된다.
 */
export function formatReviewNumber(value: string): string {
  const groups = value.match(/.{1,4}/g);
  return groups ? groups.join("-") : value;
}

/**
 * `yyyy-MM` 을 그래프 축 라벨용 `M월` 로 줄인다.
 *
 * 6개월치를 좁은 축에 늘어놓으므로 연도를 빼는데, 해가 바뀌는 1월만 `yyyy.M월` 로
 * 표시해 어느 해의 1월인지 알 수 있게 한다.
 */
export function formatYearMonthLabel(yearMonth: string): string {
  const [year, month] = yearMonth.split("-");
  if (!year || !month) return yearMonth;

  const monthNumber = Number(month);
  if (!Number.isInteger(monthNumber)) return yearMonth;

  return monthNumber === 1 ? `${year}.${monthNumber}월` : `${monthNumber}월`;
}

/** 별점 분포 막대의 채움 비율(%). 전체가 0건이면 0 */
export function toRatingBarPercentage(count: number, total: number): number {
  if (total <= 0) return 0;
  return (count / total) * 100;
}
