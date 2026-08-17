import { PRODUCT_AVAILABILITY_COPY } from "./message";

/**
 * 품절 종료 시각 표시 — `3.31 (화) 오전 10:00까지 품절`.
 *
 * 요일·오전/오후를 직접 문자열로 조립하지 않고 `Intl.DateTimeFormat` 에 맡긴다 —
 * 직접 배열을 들면 로케일이 바뀔 때 조용히 틀리고, 12시/0시 경계를 매번 다시 틀리기 때문이다.
 */
const SOLD_OUT_UNTIL_FORMATTER = new Intl.DateTimeFormat("ko-KR", {
  month: "numeric",
  day: "numeric",
  weekday: "short",
  hour: "numeric",
  minute: "2-digit",
  hour12: true,
});

export function formatSoldOutUntil(value: string | null | undefined): string | undefined {
  if (!value) return undefined;

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;

  return `${SOLD_OUT_UNTIL_FORMATTER.format(date)}${PRODUCT_AVAILABILITY_COPY.SOLD_OUT_UNTIL_SUFFIX}`;
}

const PRICE_FORMATTER = new Intl.NumberFormat("ko-KR");

/** 원화 표기. `formatCurrency`(USD 기본)는 로케일이 달라 이 화면에서는 쓰지 않는다 */
export function formatPrice(value: number): string {
  return `${PRICE_FORMATTER.format(value)}원`;
}

/** 시 Select 표시 — `오전 9시` / `오후 1시`. 0시는 `오전 12시`, 12시는 `오후 12시`다 */
export function formatHourLabel(hour: number): string {
  const meridiem = hour < 12 ? "오전" : "오후";
  const displayHour = hour % 12 === 0 ? 12 : hour % 12;
  return `${meridiem} ${displayHour}시`;
}

export function formatMinuteLabel(minute: number): string {
  return `${minute}분`;
}
