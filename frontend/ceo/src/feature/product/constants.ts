import type {
  AvailabilityTab,
  ExposureDaySelectionMode,
  OptionGroupMergeMode,
  ProductExposureDayType,
  ProductOptionGroupType,
  ProductReleaseTarget,
  VegetarianType,
} from "./domain";

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

// =====================================================================================
// 점주 메뉴·옵션 관리 상수 (`docs/tasks/frontend.md`)
// =====================================================================================

/** 서버 `@Size` 와 같은 값 — 폼에서 먼저 막아 400 왕복을 줄인다 */
export const PRODUCT_NAME_MAX_LENGTH = 255;
export const PRODUCT_COMPOSITION_MAX_LENGTH = 500;
export const PRODUCT_DESCRIPTION_MAX_LENGTH = 1000;
/** 중량 표기(치킨 등 법정 의무표시). 예) `조리 전 총 중량 1,200g` · `10호(951~1050g)` */
export const PRODUCT_WEIGHT_TEXT_MAX_LENGTH = 50;
export const PRODUCT_CATEGORY_NAME_MAX_LENGTH = 100;
export const PRODUCT_CATEGORY_DESCRIPTION_MAX_LENGTH = 500;
export const OPTION_GROUP_NAME_MAX_LENGTH = 100;
export const OPTION_GROUP_DESCRIPTION_MAX_LENGTH = 500;
export const OPTION_NAME_MAX_LENGTH = 100;
export const VEGETARIAN_INGREDIENTS_MAX_LENGTH = 500;
export const VEGETARIAN_DESCRIPTION_MAX_LENGTH = 1000;

/** 일괄 삭제 상한 — 서버 `@Size(max=200)` 와 같다 */
export const PRODUCT_DELETE_MAX_COUNT = 200;

/**
 * 메뉴명 특수문자 화이트리스트 (`backend.md` §2-1).
 *
 * 서버가 최종 판정(`PRODUCT_NAME_INVALID_CHARACTER`)하지만, 폼에서 먼저 걸러 주면
 * 제출 왕복 없이 어떤 글자가 문제인지 알 수 있다.
 * 한글·영문·숫자·공백에 더해 `: , . / ~ % & ( ) + [ ] ™ ®` 만 허용한다.
 */
export const PRODUCT_NAME_PATTERN = /^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\s:,./~%&()+[\]™®-]+$/;

/** 매운맛 단계. 0 은 "설정 안 함"이 아니라 "안 매움"이다 */
export const SPICINESS_OPTIONS = [
  { value: 0, label: "안 매움" },
  { value: 1, label: "약간 매움" },
  { value: 2, label: "보통 매움" },
  { value: 3, label: "많이 매움" },
  { value: 4, label: "아주 매움" },
] as const;

/** 이미지 규격 안내 (`backend.md` §7-1). 판정은 서버가 한다 */
export const PRODUCT_IMAGE_MIN_WIDTH = 1280;
export const PRODUCT_IMAGE_MIN_HEIGHT = 960;
export const PRODUCT_IMAGE_MAX_SIZE_MB = 15;
export const PRODUCT_IMAGE_ACCEPT = "image/jpeg,image/png";

/**
 * 요일 묶음. 개별 요일과 **함께 보낼 수 없다**(`PRODUCT_EXPOSURE_DAY_TYPE_MIXED`).
 */
export const EXPOSURE_PRESET_DAY_OPTIONS = [
  { value: "DAILY", label: "매일" },
  { value: "WEEKDAY", label: "평일" },
  { value: "WEEKEND", label: "주말" },
  { value: "HOLIDAY", label: "공휴일" },
] as const satisfies readonly { value: ProductExposureDayType; label: string }[];

export const EXPOSURE_INDIVIDUAL_DAY_OPTIONS = [
  { value: "MONDAY", label: "월" },
  { value: "TUESDAY", label: "화" },
  { value: "WEDNESDAY", label: "수" },
  { value: "THURSDAY", label: "목" },
  { value: "FRIDAY", label: "금" },
  { value: "SATURDAY", label: "토" },
  { value: "SUNDAY", label: "일" },
] as const satisfies readonly { value: ProductExposureDayType; label: string }[];

export const EXPOSURE_PRESET_DAY_TYPES: readonly ProductExposureDayType[] = EXPOSURE_PRESET_DAY_OPTIONS.map(
  (option) => option.value,
);

export const EXPOSURE_DAY_SELECTION_MODES = {
  PRESET: "PRESET",
  INDIVIDUAL: "INDIVIDUAL",
} as const satisfies Record<string, ExposureDaySelectionMode>;

/** 노출 시간대 Select 는 30분 단위 — 품절기간(10분)보다 거칠어도 되는 영업 스케줄 값이다 */
export const EXPOSURE_TIME_OPTIONS: readonly string[] = Array.from({ length: 48 }, (_, index) => {
  const hour = String(Math.floor(index / 2)).padStart(2, "0");
  const minute = index % 2 === 0 ? "00" : "30";
  return `${hour}:${minute}`;
});

export const VEGETARIAN_TYPE_OPTIONS = [
  { value: "VEGAN", label: "비건" },
  { value: "LACTO", label: "락토" },
  { value: "OVO", label: "오보" },
  { value: "LACTO_OVO", label: "락토오보" },
  { value: "PESCO", label: "페스코" },
] as const satisfies readonly { value: VegetarianType; label: string }[];

// =====================================================================================
// 옵션그룹 합치기 · 일회용컵 보증금 상수 (`docs/tasks/frontend.md` §2~§3)
// =====================================================================================

/** 메뉴·옵션 관리 통합 레이아웃의 탭. `?tab=` searchParam 으로 구동한다 */
export const MENU_TABS = {
  MENU: "menu",
  OPTION: "option",
} as const;

export type MenuTab = (typeof MENU_TABS)[keyof typeof MENU_TABS];

/** 합치기 화면 모드. `?mode=` searchParam 으로 구동한다(추천이 기본) */
export const OPTION_GROUP_MERGE_MODES = {
  RECOMMENDED: "RECOMMENDED",
  MANUAL: "MANUAL",
} as const satisfies Record<string, OptionGroupMergeMode>;

/** 직접 선택은 최소 2개를 골라야 기준/후보가 성립한다 */
export const OPTION_GROUP_MERGE_MIN_SELECTION = 2;

export const OPTION_GROUP_TYPES = {
  NORMAL: "NORMAL",
  CUP_DEPOSIT: "CUP_DEPOSIT",
} as const satisfies Record<string, ProductOptionGroupType>;

export const OPTION_GROUP_TYPE_OPTIONS = [
  { value: OPTION_GROUP_TYPES.NORMAL, label: "일반 옵션그룹" },
  { value: OPTION_GROUP_TYPES.CUP_DEPOSIT, label: "일회용컵 보증금" },
] as const satisfies readonly { value: ProductOptionGroupType; label: string }[];

/**
 * 컵 1개당 보증금 요율.
 *
 * 서버 `CupDepositPolicy.DEPOSIT_PER_CUP` 과 같은 값이지만 **표시 계산 전용**이다 —
 * 금액의 진실원은 서버가 스냅샷 단계에서 확정하는 `depositAmount` 이고, 여기 값은 폼에서
 * "보증금 N원" 미리보기를 즉시 보여주기 위한 것이다. 요율이 바뀌면 서버가 먼저 바뀐다.
 */
export const CUP_DEPOSIT_PER_CUP = 300;

/** 서버 `@Min(1) @Max(10)` 과 같은 값 */
export const CUP_COUNT_MIN = 1;
export const CUP_COUNT_MAX = 10;

/** 보증금 옵션그룹의 선택 개수는 서버가 강제한다 — 폼은 이 값을 고정 표시한다 */
export const CUP_DEPOSIT_FIXED_MIN_SELECT = "0";
export const CUP_DEPOSIT_FIXED_MAX_SELECT = "1";

/** 사장님 추천(대표 메뉴) 등록 상한 (`docs/tasks/menu-board-promotion/frontend.md` A-2 기준 2번) */
export const PRODUCT_REPRESENTATIVE_MAX_COUNT = 6;

// ===== 영양성분·알레르기 (법정 표시 의무) =====

/** 텍스트 항목(맛·사이즈·제공량) 최대 길이 */
export const NUTRITION_TEXT_MAX_LENGTH = 50;

/**
 * 필수 5종.
 *
 * 전부 채우거나 전부 비운다 — 일부만 채운 영양성분 표시는 법적으로 의미가 없고 오히려 위반이다.
 * 순서는 화면 표시 순서이자 PDF 열거 순서다.
 */
export const NUTRITION_REQUIRED_KEYS = ["calorie", "sugars", "protein", "saturatedFat", "natrium"] as const;

export type NutritionRequiredKey = (typeof NUTRITION_REQUIRED_KEYS)[number];

/** 선택 수치 9종 중 수치형 5종. 나머지 4종(맛·사이즈·제공량 2)은 텍스트다 */
export const NUTRITION_OPTIONAL_NUMERIC_KEYS = ["carbohydrate", "cholesterol", "fat", "transFat", "caffeine"] as const;

export type NutritionOptionalNumericKey = (typeof NUTRITION_OPTIONAL_NUMERIC_KEYS)[number];

export type NutritionNumericKey = NutritionRequiredKey | NutritionOptionalNumericKey;

/** 수치 입력란 옆에 붙는 단위. 단위 없이 숫자만 두면 무엇을 적는지 알 수 없다 */
export const NUTRITION_UNIT: Record<NutritionNumericKey, string> = {
  calorie: "kcal",
  sugars: "g",
  protein: "g",
  saturatedFat: "g",
  natrium: "mg",
  carbohydrate: "g",
  cholesterol: "mg",
  fat: "g",
  transFat: "g",
  caffeine: "mg",
};

/** 0 이상 정수만 허용한다. 음수·소수는 영양성분 표기 단위로 쓰지 않는다 */
export const NUTRITION_NON_NEGATIVE_INT_PATTERN = /^\d+$/;
