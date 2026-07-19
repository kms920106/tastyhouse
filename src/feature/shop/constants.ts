// 가게 도메인 Enum/상수.
// 아래 enum 값들(dayType/closedDayType/amenity/foodType/orderMethod)은 백엔드 실제 enum 기준(2026-07-19 확인)이다.
// 서버가 목록 응답에 한글 라벨(description/displayName)을 내려주는 항목은 표시에 그 값을 사용하고,
// 아래 *_LABEL 맵은 등록/수정 드롭다운의 옵션 카탈로그 렌더링 용도로만 사용한다.

export const SHOP_NAME_MAX = 100;
export const ADDRESS_MAX = 200;
export const PHOTO_CATEGORY_NAME_MAX = 100;
export const EDITOR_CHOICE_TITLE_MAX = 200;
export const EDITOR_CHOICE_CONTENT_MAX = 2000;
export const AMENITY_DISPLAY_NAME_MAX = 50;
export const FOOD_TYPE_DISPLAY_NAME_MAX = 50;
export const TAG_NAME_MAX = 50;

// dayType — 백엔드 DayType enum 기준(2026-07-19 확인)
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

// closedDayType — 백엔드 ClosedDayType enum 기준(2026-07-19 확인)
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

// amenity — 백엔드 실제 GET /api/shops/v1/amenity-categories 응답 기준(2026-07-19 확인)
export const AMENITY_OPTIONS = [
  "PARKING",
  "RESTROOM",
  "RESERVATION",
  "BABY_CHAIR",
  "PET_FRIENDLY",
  "OUTLET",
  "TAKEOUT",
  "DELIVERY",
] as const;
export type AmenityOption = (typeof AMENITY_OPTIONS)[number];

export const AMENITY_LABEL: Record<AmenityOption, string> = {
  PARKING: "주차",
  RESTROOM: "내부화장실",
  RESERVATION: "예약",
  BABY_CHAIR: "아기의자",
  PET_FRIENDLY: "애견동반",
  OUTLET: "개별 콘센트",
  TAKEOUT: "포장",
  DELIVERY: "배달",
};

// foodType — 백엔드 실제 GET /api/shops/v1/food-type-categories 응답 기준(2026-07-19 확인)
export const FOOD_TYPE_OPTIONS = ["KOREAN", "JAPANESE", "WESTERN", "CHINESE", "WORLD", "SNACK", "BAR", "CAFE"] as const;
export type FoodTypeOption = (typeof FOOD_TYPE_OPTIONS)[number];

export const FOOD_TYPE_LABEL: Record<FoodTypeOption, string> = {
  KOREAN: "한식",
  JAPANESE: "일식",
  WESTERN: "양식",
  CHINESE: "중식",
  WORLD: "세계음식",
  SNACK: "분식",
  BAR: "주점",
  CAFE: "카페",
};

// orderMethod — 백엔드 OrderMethod enum 기준(2026-07-19 확인)
export const ORDER_METHOD_OPTIONS = ["TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"] as const;
export type OrderMethodOption = (typeof ORDER_METHOD_OPTIONS)[number];

export const ORDER_METHOD_LABEL: Record<OrderMethodOption, string> = {
  TABLE: "테이블 오더",
  RESERVATION: "예약",
  DELIVERY: "배달",
  TAKEOUT: "포장",
};

// 가게 상세 페이지 탭 정의
export const SHOP_DETAIL_TABS = {
  BUSINESS_HOURS: "business-hours",
  CLASSIFICATION: "classification",
  IMAGES: "images",
} as const;
