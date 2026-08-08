// 가게 도메인 Enum/상수.
// docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md 를 유일한 근거로 삼는다.
// 서버가 목록 응답에 한글 라벨(description/displayName)을 내려주는 항목은 표시에 그 값을 사용하고,
// 아래 *_LABEL 맵은 등록/수정 드롭다운의 옵션 카탈로그 렌더링 용도로만 사용한다.

// ===== 입력 제한 =====

export const SHOP_INTRODUCTION_MAX = 500;
export const SHOP_DIRECTIONS_MAX = 200;
export const CONTENT_BOARD_DESCRIPTION_MAX = 50;
export const PHONE_NUMBER_MAX_COUNT = 10;
export const CONTENT_BOARD_MAX_COUNT = 4;
export const TEMPORARY_CLOSURE_MAX_DAYS = 30;
export const REGULAR_CLOSED_DAY_MAX_COUNT = 15;

/** 최소주문금액 미설정 값 — 제한 없음 */
export const MIN_ORDER_AMOUNT_UNSET = 0;
/** 최소주문금액을 설정할 때의 하한(원) */
export const MIN_ORDER_AMOUNT_LOWER_BOUND = 5000;
/** 최소주문금액을 설정할 때의 상한(원) */
export const MIN_ORDER_AMOUNT_UPPER_BOUND = 30000;
/** 최소주문금액 입력 단위(원) */
export const MIN_ORDER_AMOUNT_STEP = 1000;

/** 기본 배달팁 구간 최대 개수 */
export const DELIVERY_TIP_TIER_MAX_COUNT = 3;
/** 기본 배달팁 상한(원) — 미만만 허용 */
export const DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE = 5000;
/** 추가 배달팁 상한(원) — 이하 허용 */
export const DELIVERY_TIP_EXTRA_UPPER_BOUND = 10000;
/** 배달팁 미설정 값 */
export const DELIVERY_TIP_UNSET = 0;
/** 기본배달거리 선택지(m) */
export const DELIVERY_TIP_BASE_DISTANCE_OPTIONS = [1000, 1500, 2000, 2500, 3000] as const;
/** 추가 거리 할증 단위별 허용 금액 범위(원) */
export const DELIVERY_TIP_SURCHARGE_RULES = {
  PER_100M: { unitMeters: 100, min: 100, max: 300 },
  PER_500M: { unitMeters: 500, min: 100, max: 1500 },
} as const;
/** 추가 배달팁 방식 */
export const EXTRA_DELIVERY_TIP_TYPES = ["NONE", "DISTANCE", "REGION"] as const;
/** 시간별 배달팁에서 선택할 수 없는 요일 구분 — 일요일은 공휴일 설정 대상이 아니라 시간별로 처리한다 */
export const DELIVERY_TIP_SCHEDULE_DISALLOWED_DAY_TYPES = ["SUNDAY", "HOLIDAY"] as const;

/** 추가 배달팁 방식 라벨 */
export const EXTRA_DELIVERY_TIP_TYPE_LABEL: Record<(typeof EXTRA_DELIVERY_TIP_TYPES)[number], string> = {
  NONE: "사용 안 함",
  DISTANCE: "거리별",
  REGION: "지역별",
};

/** 추가 거리 할증 단위 라벨 */
export const DELIVERY_TIP_SURCHARGE_UNIT_LABEL: Record<keyof typeof DELIVERY_TIP_SURCHARGE_RULES, string> = {
  PER_100M: "100m당",
  PER_500M: "500m당",
};

/** 시간별 배달팁 요일 칩 — 금지 요일(SUNDAY·HOLIDAY)을 제외한 목록 */
export const DELIVERY_TIP_SCHEDULE_DAY_TYPE_OPTIONS = [
  "DAILY",
  "WEEKDAY",
  "WEEKEND",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
] as const;

/** 노출 위치가 허용되는 실제 위치 기준 반경(m) */
export const DISPLAY_LOCATION_MAX_DISTANCE_M = 1000;

/** 가게 상표 이미지 최대 용량 (900KB) */
export const TRADEMARK_IMAGE_MAX_BYTES = 900 * 1024;
/** 가게 상표 이미지 최소 한 변 길이(px) — 종횡비 1:1 필수 */
export const TRADEMARK_IMAGE_MIN_SIZE = 560;
/** 대표이미지 최대 용량 (10MB) */
export const THUMBNAIL_IMAGE_MAX_BYTES = 10 * 1024 * 1024;
/** 대표이미지 최소 한 변 길이(px) */
export const THUMBNAIL_IMAGE_MIN_SIZE = 700;
/** 콘텐츠보드 이미지(JPG/PNG) 최소 한 변 길이(px) */
export const CONTENT_BOARD_IMAGE_MIN_SIZE = 700;
/** 콘텐츠보드 이미지/GIF 최대 용량 (10MB) */
export const CONTENT_BOARD_IMAGE_MAX_BYTES = 10 * 1024 * 1024;
/** 콘텐츠보드 GIF 최소 한 변 길이(px) */
export const CONTENT_BOARD_GIF_MIN_SIZE = 250;

/** 영업시간 입력 단위 (분) */
export const TIME_STEP_MINUTES = 5;
/** 하루 영업시간 최소 길이 (분) */
export const BUSINESS_HOUR_MIN_MINUTES = 60;
/** 하루 영업시간 최대 길이 (분) — 23시간 55분 */
export const BUSINESS_HOUR_MAX_MINUTES = 23 * 60 + 55;

// ===== 화면 탭 =====

export const SHOP_MANAGE_TABS = {
  BASIC: "basic",
  OPERATION: "operation",
  ORDER: "order",
} as const;
export type ShopManageTab = (typeof SHOP_MANAGE_TABS)[keyof typeof SHOP_MANAGE_TABS];

// ===== dayType =====

export const DAY_TYPE_OPTIONS = [
  "DAILY",
  "WEEKDAY",
  "WEEKEND",
  "HOLIDAY",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
] as const;
export type DayTypeOption = (typeof DAY_TYPE_OPTIONS)[number];

export const DAY_TYPE_LABEL: Record<DayTypeOption, string> = {
  DAILY: "매일",
  WEEKDAY: "평일",
  WEEKEND: "주말",
  HOLIDAY: "공휴일",
  MONDAY: "월요일",
  TUESDAY: "화요일",
  WEDNESDAY: "수요일",
  THURSDAY: "목요일",
  FRIDAY: "금요일",
  SATURDAY: "토요일",
  SUNDAY: "일요일",
};

/** 요일별 편집 화면에서 행으로 나열하는 실제 요일 (매일/평일/주말/공휴일 같은 묶음 제외) */
export const WEEKDAY_OPTIONS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"] as const;
export type WeekdayOption = (typeof WEEKDAY_OPTIONS)[number];

// ===== closedDayType =====

export const CLOSED_DAY_TYPE_OPTIONS = [
  "NO_CLOSED_DAYS",
  "EVERY_WEEK_MONDAY",
  "EVERY_WEEK_TUESDAY",
  "EVERY_WEEK_WEDNESDAY",
  "EVERY_WEEK_THURSDAY",
  "EVERY_WEEK_FRIDAY",
  "EVERY_WEEK_SATURDAY",
  "EVERY_WEEK_SUNDAY",
  "EVERY_MONTH_FIRST_WEEK_MONDAY",
  "EVERY_MONTH_FIRST_WEEK_TUESDAY",
  "EVERY_MONTH_FIRST_WEEK_WEDNESDAY",
  "EVERY_MONTH_FIRST_WEEK_THURSDAY",
  "EVERY_MONTH_FIRST_WEEK_FRIDAY",
  "EVERY_MONTH_FIRST_WEEK_SATURDAY",
  "EVERY_MONTH_FIRST_WEEK_SUNDAY",
  "EVERY_MONTH_SECOND_WEEK_MONDAY",
  "EVERY_MONTH_SECOND_WEEK_TUESDAY",
  "EVERY_MONTH_SECOND_WEEK_WEDNESDAY",
  "EVERY_MONTH_SECOND_WEEK_THURSDAY",
  "EVERY_MONTH_SECOND_WEEK_FRIDAY",
  "EVERY_MONTH_SECOND_WEEK_SATURDAY",
  "EVERY_MONTH_SECOND_WEEK_SUNDAY",
  "EVERY_MONTH_THIRD_WEEK_MONDAY",
  "EVERY_MONTH_THIRD_WEEK_TUESDAY",
  "EVERY_MONTH_THIRD_WEEK_WEDNESDAY",
  "EVERY_MONTH_THIRD_WEEK_THURSDAY",
  "EVERY_MONTH_THIRD_WEEK_FRIDAY",
  "EVERY_MONTH_THIRD_WEEK_SATURDAY",
  "EVERY_MONTH_THIRD_WEEK_SUNDAY",
  "EVERY_MONTH_FOURTH_WEEK_MONDAY",
  "EVERY_MONTH_FOURTH_WEEK_TUESDAY",
  "EVERY_MONTH_FOURTH_WEEK_WEDNESDAY",
  "EVERY_MONTH_FOURTH_WEEK_THURSDAY",
  "EVERY_MONTH_FOURTH_WEEK_FRIDAY",
  "EVERY_MONTH_FOURTH_WEEK_SATURDAY",
  "EVERY_MONTH_FOURTH_WEEK_SUNDAY",
  "EVERY_MONTH_LAST_WEEK_MONDAY",
  "EVERY_MONTH_LAST_WEEK_TUESDAY",
  "EVERY_MONTH_LAST_WEEK_WEDNESDAY",
  "EVERY_MONTH_LAST_WEEK_THURSDAY",
  "EVERY_MONTH_LAST_WEEK_FRIDAY",
  "EVERY_MONTH_LAST_WEEK_SATURDAY",
  "EVERY_MONTH_LAST_WEEK_SUNDAY",
] as const;
export type ClosedDayTypeOption = (typeof CLOSED_DAY_TYPE_OPTIONS)[number];

export const CLOSED_DAY_TYPE_LABEL: Record<ClosedDayTypeOption, string> = {
  NO_CLOSED_DAYS: "연중무휴",
  EVERY_WEEK_MONDAY: "매주 월요일",
  EVERY_WEEK_TUESDAY: "매주 화요일",
  EVERY_WEEK_WEDNESDAY: "매주 수요일",
  EVERY_WEEK_THURSDAY: "매주 목요일",
  EVERY_WEEK_FRIDAY: "매주 금요일",
  EVERY_WEEK_SATURDAY: "매주 토요일",
  EVERY_WEEK_SUNDAY: "매주 일요일",
  EVERY_MONTH_FIRST_WEEK_MONDAY: "매달 첫째 주 월요일",
  EVERY_MONTH_FIRST_WEEK_TUESDAY: "매달 첫째 주 화요일",
  EVERY_MONTH_FIRST_WEEK_WEDNESDAY: "매달 첫째 주 수요일",
  EVERY_MONTH_FIRST_WEEK_THURSDAY: "매달 첫째 주 목요일",
  EVERY_MONTH_FIRST_WEEK_FRIDAY: "매달 첫째 주 금요일",
  EVERY_MONTH_FIRST_WEEK_SATURDAY: "매달 첫째 주 토요일",
  EVERY_MONTH_FIRST_WEEK_SUNDAY: "매달 첫째 주 일요일",
  EVERY_MONTH_SECOND_WEEK_MONDAY: "매달 둘째 주 월요일",
  EVERY_MONTH_SECOND_WEEK_TUESDAY: "매달 둘째 주 화요일",
  EVERY_MONTH_SECOND_WEEK_WEDNESDAY: "매달 둘째 주 수요일",
  EVERY_MONTH_SECOND_WEEK_THURSDAY: "매달 둘째 주 목요일",
  EVERY_MONTH_SECOND_WEEK_FRIDAY: "매달 둘째 주 금요일",
  EVERY_MONTH_SECOND_WEEK_SATURDAY: "매달 둘째 주 토요일",
  EVERY_MONTH_SECOND_WEEK_SUNDAY: "매달 둘째 주 일요일",
  EVERY_MONTH_THIRD_WEEK_MONDAY: "매달 셋째 주 월요일",
  EVERY_MONTH_THIRD_WEEK_TUESDAY: "매달 셋째 주 화요일",
  EVERY_MONTH_THIRD_WEEK_WEDNESDAY: "매달 셋째 주 수요일",
  EVERY_MONTH_THIRD_WEEK_THURSDAY: "매달 셋째 주 목요일",
  EVERY_MONTH_THIRD_WEEK_FRIDAY: "매달 셋째 주 금요일",
  EVERY_MONTH_THIRD_WEEK_SATURDAY: "매달 셋째 주 토요일",
  EVERY_MONTH_THIRD_WEEK_SUNDAY: "매달 셋째 주 일요일",
  EVERY_MONTH_FOURTH_WEEK_MONDAY: "매달 넷째 주 월요일",
  EVERY_MONTH_FOURTH_WEEK_TUESDAY: "매달 넷째 주 화요일",
  EVERY_MONTH_FOURTH_WEEK_WEDNESDAY: "매달 넷째 주 수요일",
  EVERY_MONTH_FOURTH_WEEK_THURSDAY: "매달 넷째 주 목요일",
  EVERY_MONTH_FOURTH_WEEK_FRIDAY: "매달 넷째 주 금요일",
  EVERY_MONTH_FOURTH_WEEK_SATURDAY: "매달 넷째 주 토요일",
  EVERY_MONTH_FOURTH_WEEK_SUNDAY: "매달 넷째 주 일요일",
  EVERY_MONTH_LAST_WEEK_MONDAY: "매달 마지막 주 월요일",
  EVERY_MONTH_LAST_WEEK_TUESDAY: "매달 마지막 주 화요일",
  EVERY_MONTH_LAST_WEEK_WEDNESDAY: "매달 마지막 주 수요일",
  EVERY_MONTH_LAST_WEEK_THURSDAY: "매달 마지막 주 목요일",
  EVERY_MONTH_LAST_WEEK_FRIDAY: "매달 마지막 주 금요일",
  EVERY_MONTH_LAST_WEEK_SATURDAY: "매달 마지막 주 토요일",
  EVERY_MONTH_LAST_WEEK_SUNDAY: "매달 마지막 주 일요일",
};

// ===== orderMethod =====

export const ORDER_METHOD_OPTIONS = ["TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"] as const;
export type OrderMethodOption = (typeof ORDER_METHOD_OPTIONS)[number];

export const ORDER_METHOD_LABEL: Record<OrderMethodOption, string> = {
  TABLE: "테이블 오더",
  RESERVATION: "예약",
  DELIVERY: "배달",
  TAKEOUT: "포장",
};

// ===== 가게 상태 =====

export const SHOP_STATUS_OPTIONS = ["OPEN", "HIDDEN"] as const;
export type ShopStatusOption = (typeof SHOP_STATUS_OPTIONS)[number];

export const SHOP_STATUS_LABEL: Record<ShopStatusOption, string> = {
  OPEN: "영업중",
  HIDDEN: "노출정지",
};

// ===== 이미지 변경 요청 승인 상태 =====

export const APPROVAL_STATUS_OPTIONS = ["PENDING", "APPROVED", "REJECTED"] as const;
export type ApprovalStatusOption = (typeof APPROVAL_STATUS_OPTIONS)[number];

export const APPROVAL_STATUS_LABEL: Record<ApprovalStatusOption, string> = {
  PENDING: "승인 대기",
  APPROVED: "승인 완료",
  REJECTED: "반려",
};

// ===== 콘텐츠보드 =====

export const CONTENT_BOARD_TYPE_OPTIONS = ["IMAGE", "GIF", "VIDEO"] as const;
export type ContentBoardTypeOption = (typeof CONTENT_BOARD_TYPE_OPTIONS)[number];

export const CONTENT_BOARD_TYPE_LABEL: Record<ContentBoardTypeOption, string> = {
  IMAGE: "이미지",
  GIF: "GIF",
  VIDEO: "동영상",
};

export const CONTENT_BOARD_TOPIC_OPTIONS = ["EXTERIOR", "INTERIOR", "FOOD_STORY", "NEWS"] as const;
export type ContentBoardTopicOption = (typeof CONTENT_BOARD_TOPIC_OPTIONS)[number];

export const CONTENT_BOARD_TOPIC_LABEL: Record<ContentBoardTopicOption, string> = {
  EXTERIOR: "가게 외부",
  INTERIOR: "가게 내부",
  FOOD_STORY: "음식 스토리",
  NEWS: "가게 소식",
};

// ===== 영업임시중지 =====

export const SUSPENSION_REASON_OPTIONS = [
  "EARLY_CLOSE",
  "OPEN_DELAY",
  "SHOP_CIRCUMSTANCE",
  "UNREACHABLE",
  "TERMINATION_REQUEST",
  "BAD_WEATHER",
] as const;
export type SuspensionReasonOption = (typeof SUSPENSION_REASON_OPTIONS)[number];

export const SUSPENSION_REASON_LABEL: Record<SuspensionReasonOption, string> = {
  EARLY_CLOSE: "조기종료",
  OPEN_DELAY: "오픈지연",
  SHOP_CIRCUMSTANCE: "가게사정",
  UNREACHABLE: "연락불가",
  TERMINATION_REQUEST: "해지요청",
  BAD_WEATHER: "기상악화",
};

// ===== 위생 인증 =====

export const HYGIENE_BADGE_TYPE_OPTIONS = ["FOOD_SAFETY_CERTIFIED", "CESCO_BLUE", "CESCO_WHITE"] as const;
export type HygieneBadgeTypeOption = (typeof HYGIENE_BADGE_TYPE_OPTIONS)[number];

export const HYGIENE_BADGE_TYPE_LABEL: Record<HygieneBadgeTypeOption, string> = {
  FOOD_SAFETY_CERTIFIED: "식품안심업소",
  CESCO_BLUE: "블루세스코",
  CESCO_WHITE: "화이트세스코",
};
