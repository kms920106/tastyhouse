// 가게 도메인 Enum/상수.
// docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md 를 유일한 근거로 삼는다.
// 서버가 목록 응답에 한글 라벨(description/displayName)을 내려주는 항목은 표시에 그 값을 사용하고,
// 아래 *_LABEL 맵은 등록/수정 드롭다운의 옵션 카탈로그 렌더링 용도로만 사용한다.

// ===== 입력 제한 =====

export const SHOP_INTRODUCTION_MAX = 500;
export const SHOP_DIRECTIONS_MAX = 200;
export const SHOP_RIDER_VISIT_GUIDE_MAX = 200;
export const SHOP_RIDER_PICKUP_DETAIL_ADDRESS_MAX = 100;
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

// ===== 배달가능지역 · 배달지역 조정 신청 =====

/** 배달가능지역 등록 상한 — 백엔드 `ShopDeliveryAreaPolicy.MAX_DELIVERY_AREA_COUNT` 와 일치시킨다 */
export const DELIVERY_AREA_MAX_COUNT = 500;
/** bulk 추가·삭제 한 번에 보낼 수 있는 행정동 개수 — 백엔드 `MAX_BULK_SIZE` 와 일치 */
export const DELIVERY_AREA_BULK_MAX_SIZE = 500;
/** 행정동 검색 한 번에 가져올 건수 */
export const ADMIN_DONG_SEARCH_SIZE = 20;
/** 검색어 입력이 멈춘 뒤 조회하기까지의 지연(ms) */
export const SEARCH_DEBOUNCE_MS = 300;

// ===== 배달지역 지도 편집 =====

/** 반경 설정 하한(km) — 백엔드 `@Min(500)`(m) 과 일치 */
export const DELIVERY_AREA_MIN_RADIUS_KM = 0.5;
/** 반경 설정 상한(km) — 가이드의 배달지역 최대 7km */
export const DELIVERY_AREA_MAX_RADIUS_KM = 7;
/** 반경 슬라이더 기본값(km) */
export const DELIVERY_AREA_DEFAULT_RADIUS_KM = 3;
/** 반경 입력 단위(km) */
export const DELIVERY_AREA_RADIUS_STEP_KM = 0.5;
/** 가게배달 기본 노출 반경(km) — 안내 표시 전용이며 배달지역 판정에는 쓰지 않는다 */
export const DELIVERY_AREA_EXPOSURE_RADIUS_KM = 4;
/** 도형 링 개수 상한 — 백엔드 `MAX_RINGS` 와 일치 */
export const DELIVERY_AREA_MAX_RINGS = 20;
/** 도형 총 정점 수 상한 — 백엔드 `MAX_VERTICES` 와 일치 */
export const DELIVERY_AREA_MAX_VERTICES = 5000;
/**
 * 카카오 지도 줌 레벨 범위 — 값이 작을수록 확대다.
 *
 * 공식 문서 기준 ROADMAP 은 1~14, SKYVIEW/HYBRID 는 0~14 다. 이 화면은 기본 ROADMAP 만 쓰므로
 * 하한을 1로 둔다. 위성 지도를 도입하면 이 값을 지도 타입별로 갈라야 한다.
 */
export const KAKAO_MAP_MIN_LEVEL = 1;
export const KAKAO_MAP_MAX_LEVEL = 14;
/** 이보다 축소하면 경계를 로드하지 않는다 — 광역 뷰에서 수천 건을 받지 않기 위한 가드 */
export const BOUNDARY_MIN_ZOOM_LEVEL = 6;
/** 지도 이동이 멈춘 뒤 경계를 조회하기까지의 지연(ms) */
export const BOUNDARY_FETCH_DEBOUNCE_MS = 200;
/** draft 임시 저장(localStorage) 지연(ms) */
export const DELIVERY_AREA_DRAFT_SAVE_DEBOUNCE_MS = 500;
/** undo/redo 스택 최대 깊이 */
export const DELIVERY_AREA_HISTORY_LIMIT = 50;

/** 편집 모드 — 이동(지도 조작) / 그리기 / 지우기 */
export const DELIVERY_AREA_MODE_OPTIONS = ["PAN", "PAINT", "ERASE"] as const;
export type DeliveryAreaMode = (typeof DELIVERY_AREA_MODE_OPTIONS)[number];

export const DELIVERY_AREA_MODE_LABEL: Record<DeliveryAreaMode, string> = {
  PAN: "이동",
  PAINT: "그리기",
  ERASE: "지우기",
};

export const BRUSH_SIZE_OPTIONS = ["S", "M", "L"] as const;
export type BrushSizeOption = (typeof BRUSH_SIZE_OPTIONS)[number];

export const BRUSH_SIZE_LABEL: Record<BrushSizeOption, string> = {
  S: "작게",
  M: "보통",
  L: "크게",
};

/**
 * 브러시 반지름(px) — 지리 거리가 아니라 화면 좌표계 기준이다.
 * 확대하면 실제 지리 반경이 줄어 정밀 편집이 되고, 축소하면 넓게 칠해진다.
 */
export const BRUSH_SIZE_RADIUS_PX: Record<BrushSizeOption, number> = { S: 16, M: 32, L: 56 };

/** 사업자등록번호 — 하이픈 제외 자릿수 */
export const BUSINESS_NUMBER_LENGTH = 10;
/** 배달지역 중첩 사유 최대 길이 */
export const ADJUSTMENT_REASON_MAX = 1000;
/** 상대 가맹점 상호명·가맹본부명 최대 길이 */
export const ADJUSTMENT_NAME_MAX = 255;

/**
 * 배달지역 조정 신청 상태 — 백엔드 `DeliveryAreaAdjustmentStatus` enum 5종과 1:1 로 대응한다.
 * 여기서 상태를 빠뜨리면 `_LABEL` 조회가 `undefined` 가 되어 배지가 빈 채로 렌더되므로,
 * enum 이 늘어나면 이 배열과 아래 라벨표를 함께 갱신한다.
 */
export const DELIVERY_AREA_ADJUSTMENT_STATUS_OPTIONS = [
  "PENDING",
  "IN_PROGRESS",
  "COMPLETED",
  "REJECTED",
  "CANCELED",
] as const;
export type DeliveryAreaAdjustmentStatusOption = (typeof DELIVERY_AREA_ADJUSTMENT_STATUS_OPTIONS)[number];

export const DELIVERY_AREA_ADJUSTMENT_STATUS_LABEL: Record<DeliveryAreaAdjustmentStatusOption, string> = {
  PENDING: "접수 대기",
  IN_PROGRESS: "조정 중",
  COMPLETED: "조정 완료",
  REJECTED: "반려",
  CANCELED: "취소",
};

/** 아직 종결되지 않은 상태 — 중복 신청을 막는 판정에 쓴다 */
export const DELIVERY_AREA_ADJUSTMENT_OPEN_STATUSES: readonly DeliveryAreaAdjustmentStatusOption[] = [
  "PENDING",
  "IN_PROGRESS",
];

/** 동의서 첨부 허용 MIME — 이미지 스캔본과 PDF 를 모두 받는다 */
export const ALLOWED_CONSENT_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"] as const;

// ===== 메뉴모음컷 · 주문안내 =====

/** 메뉴모음컷 등록 상한 (`docs/tasks/menu-board-promotion/frontend.md` A-1) */
export const MENU_COLLECTION_MAX_COUNT = 6;

/** 메뉴 이미지와 같은 규격이라 accept 도 같다 — 판정은 서버가 한다 */
export const MENU_COLLECTION_IMAGE_ACCEPT = "image/jpeg,image/png";

/** 서버 `@Size(max = 500)` 과 같은 값 */
export const ORDER_NOTICE_CONTENT_MAX = 500;
