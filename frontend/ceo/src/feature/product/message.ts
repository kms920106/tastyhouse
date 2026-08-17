import { SOLD_OUT_UNTIL_MAX_DAYS, SOLD_OUT_UNTIL_MIN_MINUTES } from "./constants";

/**
 * 점주 품절·숨김 한국어 문구.
 *
 * 컴포넌트·액션에 문자열을 인라인하지 않는다(`frontend/ceo/CLAUDE.md`).
 */
export const PRODUCT_AVAILABILITY_COPY = {
  ENTRY_TITLE: "품절·숨김",
  PAGE_TITLE: "품절·숨김 설정",
  PAGE_DESCRIPTION: "재료가 떨어졌을 때 메뉴와 옵션을 한 번에 찾아 품절·숨김을 설정할 수 있습니다.",

  SEARCH_PLACEHOLDER: "메뉴·옵션 검색",
  SEARCH_SUBMIT: "검색",
  TAB_MENU: "메뉴",
  TAB_OPTION: "옵션",
  FILTER_SOLD_OUT: "품절보기",
  FILTER_HIDDEN: "숨김보기",
  SELECT_ALL: "전체 선택",

  BULK_PREFIX: "선택한 항목",
  BULK_SUFFIX: "개를",
  BUTTON_HIDE: "숨김",
  BUTTON_SOLD_OUT: "품절",
  BUTTON_RELEASE: "해제",
  BUTTON_RELEASE_SOLD_OUT: "품절 해제",
  BUTTON_RELEASE_HIDDEN: "숨김 해제",
  BUTTON_RELEASE_ALL: "품절·숨김 해제",
  BUTTON_CHANGE_PERIOD: "기간변경",
  BUTTON_SET_PERIOD: "기간설정",

  DIALOG_PERIOD_TITLE: "품절기간 변경",
  DIALOG_PERIOD_DESCRIPTION: `선택한 항목의 품절 종료 시각을 지정합니다. 지금부터 ${SOLD_OUT_UNTIL_MIN_MINUTES}분 이후 ~ ${SOLD_OUT_UNTIL_MAX_DAYS}일 이내로 설정할 수 있습니다.`,
  DIALOG_PERIOD_DATE_LABEL: "날짜",
  DIALOG_PERIOD_HOUR_LABEL: "시",
  DIALOG_PERIOD_MINUTE_LABEL: "분",
  DIALOG_PERIOD_APPLY: "적용하기",
  DIALOG_PERIOD_CANCEL: "취소",

  BADGE_SOLD_OUT: "품절",
  BADGE_HIDDEN: "숨김",
  BADGE_REQUIRED: "필수",
  BADGE_REPRESENTATIVE: "사장님 추천",
  BADGE_COMMON_OPTION: "공통",
  LINKED_PRODUCTS: "연결된 메뉴",
  OPTION_SELECT_RANGE_MIN: "최소",
  OPTION_SELECT_RANGE_MAX: "최대",
  OPTION_SELECT_RANGE_UNIT: "개",
  /** 개수 단위. 옵션 선택 범위(`최소 N개`)와 의미가 달라 상수를 나눈다 */
  COUNT_UNIT: "개",
  ADDITIONAL_PRICE_PREFIX: "+",
  NO_CATEGORY: "분류 없음",

  SOLD_OUT_UNTIL_SUFFIX: "까지 품절",
  SOLD_OUT_INDEFINITE: "기간이 지정되지 않은 품절입니다.",

  FAILURE_NOTICE_TITLE: "처리되지 않은 항목",
  FAILURE_NOTICE_DESCRIPTION: "아래 항목은 조건에 맞지 않아 처리되지 않았습니다. 나머지 항목은 정상 적용되었습니다.",
  FAILURE_NOTICE_DISMISS: "닫기",

  EMPTY: "등록된 메뉴가 없습니다.",
  EMPTY_OPTION: "등록된 옵션이 없습니다.",
  EMPTY_FILTERED: "조건에 맞는 항목이 없습니다.",
  EMPTY_DESCRIPTION: "검색어나 필터를 바꿔 다시 확인해 주세요.",
} as const;

export const PRODUCT_MESSAGE = {
  INVALID_INPUT: "입력값을 다시 확인해 주세요.",
  TARGET_REQUIRED: "대상을 1개 이상 선택해 주세요.",

  SOLD_OUT_SUCCESS: "품절 처리했습니다.",
  HIDE_SUCCESS: "숨김 처리했습니다.",
  RELEASE_SUCCESS: "해제했습니다.",
  PERIOD_CHANGE_SUCCESS: "품절 기간을 변경했습니다.",

  SOLD_OUT_FAILED: "품절 처리에 실패했습니다.",
  HIDE_FAILED: "숨김 처리에 실패했습니다.",
  RELEASE_FAILED: "해제에 실패했습니다.",
  PERIOD_CHANGE_FAILED: "품절 기간을 변경하지 못했습니다.",

  PARTIAL_FAILURE: "일부 항목은 처리되지 않았습니다.",
  /** 부분실패 토스트 제목. 문구를 컴포넌트에 인라인하지 않기 위해 함수 상수로 둔다 */
  PARTIAL_FAILURE_SUMMARY: (succeeded: number, failed: number) => `${succeeded}개 처리, ${failed}개 실패`,
  CHANGE_FAILED: "처리에 실패했습니다.",

  SHOP_LIST_LOAD_FAILED: "내 가게 목록을 불러오지 못했습니다.",
  LOAD_FAILED: "목록을 불러오지 못했습니다.",
  SHOP_ACCESS_DENIED: "이 가게에 접근할 권한이 없습니다.",
  SHOP_NOT_FOUND: "가게를 찾을 수 없습니다.",
  SHOP_EMPTY: "등록된 가게가 없습니다.",
} as const;

/** 폼 검증 문구. 서버도 같은 규칙으로 400 을 내므로 문구를 맞춘다 */
export const PRODUCT_VALIDATION_MESSAGE = {
  SOLD_OUT_UNTIL_DATE_REQUIRED: "날짜를 선택해 주세요.",
  SOLD_OUT_UNTIL_HOUR_REQUIRED: "시를 선택해 주세요.",
  SOLD_OUT_UNTIL_MINUTE_REQUIRED: "분을 선택해 주세요.",
  SOLD_OUT_UNTIL_INVALID: "올바른 일시를 선택해 주세요.",
  SOLD_OUT_UNTIL_TOO_SOON: `품절 기간은 현재 시각으로부터 ${SOLD_OUT_UNTIL_MIN_MINUTES}분 이후여야 합니다.`,
  SOLD_OUT_UNTIL_TOO_FAR: `품절 기간은 최대 ${SOLD_OUT_UNTIL_MAX_DAYS}일 이내여야 합니다.`,
  SHOP_REQUIRED: "가게를 선택해 주세요.",
} as const;
