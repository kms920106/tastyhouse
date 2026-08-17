import type { AvailabilityTab, ProductReleaseTarget } from "./domain";

/**
 * 품절 기간 경계 (`docs/tasks/backend.md` §3-3).
 *
 * `schema.ts` 가 아니라 여기에 두는 이유는 `message.ts` 의 안내 문구가 이 값을 문자열에 끼워 넣는데,
 * `schema.ts` 는 반대로 `message.ts` 를 import 하기 때문이다 — 상수를 스키마에 두면 순환 참조가 된다.
 *
 * **경계값의 최종 판정은 서버다.** 폼을 열어둔 사이 시간이 흘러 `현재+30분` 을 못 넘길 수 있으므로,
 * 클라이언트 검증은 UX 용이고 서버의 `PRODUCT_SOLD_OUT_UNTIL_TOO_SOON`(400)을 그대로 노출한다.
 */
export const SOLD_OUT_UNTIL_MIN_MINUTES = 30;
export const SOLD_OUT_UNTIL_MAX_DAYS = 7;

export const AVAILABILITY_TABS = {
  MENU: "menu",
  OPTION: "option",
} as const satisfies Record<string, AvailabilityTab>;

export const RELEASE_TARGETS = {
  SOLD_OUT: "SOLD_OUT",
  HIDDEN: "HIDDEN",
  ALL: "ALL",
} as const satisfies Record<string, ProductReleaseTarget>;

export const RELEASE_TARGET_OPTIONS = [
  { value: RELEASE_TARGETS.SOLD_OUT, label: "품절 해제" },
  { value: RELEASE_TARGETS.HIDDEN, label: "숨김 해제" },
  { value: RELEASE_TARGETS.ALL, label: "품절·숨김 해제" },
] as const satisfies readonly { value: ProductReleaseTarget; label: string }[];

export const RELEASE_TARGET_LABEL: Record<ProductReleaseTarget, string> = {
  SOLD_OUT: "품절 해제",
  HIDDEN: "숨김 해제",
  ALL: "품절·숨김 해제",
};

/** 시는 1시간 단위(원문 PDF). 표시는 `오전/오후 h시` 로 조립한다 */
export const HOUR_OPTIONS = Array.from({ length: 24 }, (_, hour) => hour) as readonly number[];

/** 분은 10분 단위(원문 PDF) */
export const MINUTE_OPTIONS = [0, 10, 20, 30, 40, 50] as const;

/** 검색어 최대 길이 — 서버 `keyword` 제약(최대 100자)과 같다 */
export const AVAILABILITY_KEYWORD_MAX_LENGTH = 100;

/** 내 가게 Select 를 채우기 위한 목록 조회 크기. 리뷰 화면과 같은 값을 쓴다 */
export const MY_SHOP_LIST_SIZE = 100;
