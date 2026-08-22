import { z } from "zod";

import {
  CUP_COUNT_MAX,
  CUP_COUNT_MIN,
  CUP_DEPOSIT_FIXED_MAX_SELECT,
  CUP_DEPOSIT_FIXED_MIN_SELECT,
  EXPOSURE_PRESET_DAY_TYPES,
  MINUTE_OPTIONS,
  OPTION_GROUP_DESCRIPTION_MAX_LENGTH,
  OPTION_GROUP_NAME_MAX_LENGTH,
  OPTION_NAME_MAX_LENGTH,
  PRODUCT_CATEGORY_DESCRIPTION_MAX_LENGTH,
  PRODUCT_CATEGORY_NAME_MAX_LENGTH,
  PRODUCT_COMPOSITION_MAX_LENGTH,
  PRODUCT_DESCRIPTION_MAX_LENGTH,
  PRODUCT_NAME_MAX_LENGTH,
  PRODUCT_NAME_PATTERN,
  SOLD_OUT_UNTIL_MAX_DAYS,
  SOLD_OUT_UNTIL_MIN_MINUTES,
  VEGETARIAN_DESCRIPTION_MAX_LENGTH,
  VEGETARIAN_INGREDIENTS_MAX_LENGTH,
} from "./constants";
import { PRODUCT_MENU_VALIDATION_MESSAGE, PRODUCT_MESSAGE, PRODUCT_VALIDATION_MESSAGE } from "./message";

const MINUTE_VALUES = MINUTE_OPTIONS.map(String);
const MINUTES_PER_DAY = 24 * 60;
const MS_PER_MINUTE = 60 * 1000;

/**
 * 폼의 날짜·시·분을 로컬 시각 `Date` 로 조립한다.
 *
 * `new Date("2026-08-17T10:00")` 같은 문자열 파싱에 기대지 않고 숫자 생성자를 쓰는 이유는,
 * 문자열 파싱이 오프셋 표기 유무에 따라 UTC 로 해석될 수 있어 KST 자정 전후에 하루가 밀리기 때문이다.
 * 반환값이 `null` 이면 실존하지 않는 날짜(예: `2026-02-31`)다.
 */
export function toSoldOutUntilDate(date: string, hour: string, minute: string): Date | null {
  const [year, month, day] = date.split("-").map(Number);
  const hourValue = Number(hour);
  const minuteValue = Number(minute);

  if (![year, month, day, hourValue, minuteValue].every(Number.isInteger)) return null;

  const assembled = new Date(year, month - 1, day, hourValue, minuteValue, 0, 0);

  // 월/일 오버플로를 다음 달로 조용히 보정하는 것을 막기 위해 왕복 확인한다.
  const isRealDate =
    assembled.getFullYear() === year && assembled.getMonth() === month - 1 && assembled.getDate() === day;

  return isRealDate ? assembled : null;
}

/**
 * 백엔드 `LocalDateTime` 이 받는 `yyyy-MM-ddTHH:mm:ss` 로 직렬화한다.
 *
 * `toISOString()` 은 UTC 로 변환해 KST 기준 시각이 9시간 밀리므로 쓰지 않는다 —
 * 서버는 타임존 없는 `LocalDateTime` 을 받으므로 로컬 시각을 그대로 조립해 보낸다.
 */
export function toLocalDateTimeString(value: Date): string {
  const pad = (part: number) => String(part).padStart(2, "0");
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}T${pad(value.getHours())}:${pad(value.getMinutes())}:00`;
}

/**
 * 품절기간 변경 폼.
 *
 * 시·분 Select 의 옵션 자체를 허용 값으로만 구성하지만, 서버 액션이 클라이언트를 거치지 않고도
 * 호출될 수 있으므로 스키마에서도 재검증한다.
 */
export const soldOutUntilSchema = z
  .object({
    date: z.string().min(1, { message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_DATE_REQUIRED }),
    hour: z.string().min(1, { message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_HOUR_REQUIRED }),
    minute: z.string().min(1, { message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_MINUTE_REQUIRED }),
  })
  .superRefine((values, ctx) => {
    const hour = Number(values.hour);
    if (!Number.isInteger(hour) || hour < 0 || hour > 23) {
      ctx.addIssue({ code: "custom", path: ["hour"], message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_INVALID });
      return;
    }

    if (!MINUTE_VALUES.includes(values.minute)) {
      ctx.addIssue({ code: "custom", path: ["minute"], message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_INVALID });
      return;
    }

    const target = toSoldOutUntilDate(values.date, values.hour, values.minute);
    if (target === null) {
      ctx.addIssue({ code: "custom", path: ["date"], message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_INVALID });
      return;
    }

    const minutesFromNow = (target.getTime() - Date.now()) / MS_PER_MINUTE;

    if (minutesFromNow < SOLD_OUT_UNTIL_MIN_MINUTES) {
      ctx.addIssue({ code: "custom", path: ["date"], message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_TOO_SOON });
      return;
    }

    if (minutesFromNow > SOLD_OUT_UNTIL_MAX_DAYS * MINUTES_PER_DAY) {
      ctx.addIssue({ code: "custom", path: ["date"], message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_TOO_FAR });
    }
  });

export type SoldOutUntilFormValues = z.infer<typeof soldOutUntilSchema>;

/** 메뉴 일괄 처리 대상 */
export const availabilityTargetSchema = z.object({
  shopId: z.number().int().positive({ message: PRODUCT_VALIDATION_MESSAGE.SHOP_REQUIRED }),
  productIds: z.array(z.number().int().positive()).min(1, { message: PRODUCT_MESSAGE.TARGET_REQUIRED }),
});

/** 옵션 일괄 처리 대상. id 만으로는 테이블을 특정할 수 없어 `optionType` 을 함께 검증한다 */
export const optionAvailabilityTargetSchema = z.object({
  shopId: z.number().int().positive({ message: PRODUCT_VALIDATION_MESSAGE.SHOP_REQUIRED }),
  options: z
    .array(
      z.object({
        optionId: z.number().int().positive(),
        optionType: z.enum(["NORMAL", "COMMON"]),
      }),
    )
    .min(1, { message: PRODUCT_MESSAGE.TARGET_REQUIRED }),
});

/** 서버가 재검증하는 `soldOutUntil` 문자열. 형식만 보고 경계값은 서버가 최종 판정한다 */
export const soldOutUntilStringSchema = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/, { message: PRODUCT_VALIDATION_MESSAGE.SOLD_OUT_UNTIL_INVALID });

export const releaseTargetSchema = z.enum(["SOLD_OUT", "HIDDEN", "ALL"]);

// =====================================================================================
// 점주 메뉴·옵션 관리 폼 스키마 (`docs/tasks/frontend.md`)
//
// 가격·개수는 `Input[type=number]` 가 문자열을 주므로 문자열로 받아 `superRefine` 에서 숫자로
// 판정한다 — `z.coerce.number()` 는 빈 문자열을 0 으로 바꿔 "미입력"과 "0원"을 구분하지 못한다.
// =====================================================================================

/** 선택 입력 숫자 필드. 비어 있으면 `undefined`, 값이 있으면 정수여야 한다 */
function parseOptionalPrice(
  value: string,
  ctx: z.RefinementCtx,
  path: string,
  { allowEmpty }: { allowEmpty: boolean },
): number | undefined {
  const trimmed = value.trim();

  if (trimmed === "") {
    if (allowEmpty) return undefined;
    ctx.addIssue({ code: "custom", path: [path], message: PRODUCT_MENU_VALIDATION_MESSAGE.ORIGINAL_PRICE_REQUIRED });
    return undefined;
  }

  const parsed = Number(trimmed);
  if (!Number.isFinite(parsed) || !Number.isInteger(parsed)) {
    ctx.addIssue({ code: "custom", path: [path], message: PRODUCT_MENU_VALIDATION_MESSAGE.PRICE_NOT_INTEGER });
    return undefined;
  }
  if (parsed < 0) {
    ctx.addIssue({ code: "custom", path: [path], message: PRODUCT_MENU_VALIDATION_MESSAGE.PRICE_NEGATIVE });
    return undefined;
  }

  return parsed;
}

/**
 * 메뉴 등록·수정 폼.
 *
 * 메뉴명 화이트리스트를 클라이언트에서도 검사하지만 **최종 판정은 서버**다
 * (금칙어·중복은 DB 를 봐야 알 수 있어 애초에 클라이언트가 판정할 수 없다).
 */
export const menuFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.NAME_REQUIRED })
      .max(PRODUCT_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.NAME_TOO_LONG })
      .regex(PRODUCT_NAME_PATTERN, { message: PRODUCT_MENU_VALIDATION_MESSAGE.NAME_INVALID_CHARACTER }),
    // Radix Select 는 빈 문자열을 값으로 쓸 수 없어 미분류를 `NONE` 센티넬로 표현한다.
    productCategoryId: z.string(),
    composition: z
      .string()
      .max(PRODUCT_COMPOSITION_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.COMPOSITION_TOO_LONG }),
    description: z
      .string()
      .max(PRODUCT_DESCRIPTION_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.DESCRIPTION_TOO_LONG }),
    originalPrice: z.string(),
    discountPrice: z.string(),
    singleServing: z.boolean(),
    representative: z.boolean(),
    spiciness: z.string(),
    ratingExcluded: z.boolean(),
  })
  .superRefine((values, ctx) => {
    const original = parseOptionalPrice(values.originalPrice, ctx, "originalPrice", { allowEmpty: false });
    const discount = parseOptionalPrice(values.discountPrice, ctx, "discountPrice", { allowEmpty: true });

    if (original !== undefined && discount !== undefined && discount > original) {
      ctx.addIssue({
        code: "custom",
        path: ["discountPrice"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.DISCOUNT_PRICE_EXCEEDS_ORIGINAL,
      });
    }
  });

export type MenuFormValues = z.infer<typeof menuFormSchema>;

/** 메뉴그룹 추가·수정 폼 */
export const menuCategoryFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.CATEGORY_NAME_REQUIRED })
    .max(PRODUCT_CATEGORY_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.CATEGORY_NAME_TOO_LONG }),
  description: z.string().max(PRODUCT_CATEGORY_DESCRIPTION_MAX_LENGTH, {
    message: PRODUCT_MENU_VALIDATION_MESSAGE.CATEGORY_DESCRIPTION_TOO_LONG,
  }),
});

export type MenuCategoryFormValues = z.infer<typeof menuCategoryFormSchema>;

export const optionGroupTypeSchema = z.enum(["NORMAL", "CUP_DEPOSIT"]);

/**
 * 옵션그룹 추가·수정 폼.
 *
 * `groupType` 은 **등록에서만 의미가 있다** — 수정은 서버가 `groupType` 을 받지 않으므로
 * 폼에서 유형 선택을 노출하지 않고, 이 스키마의 기본값(`NORMAL`)이 그대로 남는다.
 *
 * `productId` 도 **등록에서만 필수다** — 서버가 등록 시 최초 연결 메뉴로 `productId`를 요구하지만
 * (연결 0건 그룹은 고아가 된다), 수정은 이미 연결된 그룹을 대상으로 하므로 이 필드를 받지 않는다.
 * 수정 폼은 메뉴 선택 UI 자체를 노출하지 않고, `null`인 채로 액션에서 걸러낸다.
 */
export const optionGroupFormSchema = z
  .object({
    productId: z.number().int().positive().nullable(),
    name: z
      .string()
      .trim()
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_NAME_REQUIRED })
      .max(OPTION_GROUP_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_NAME_TOO_LONG }),
    description: z.string().max(OPTION_GROUP_DESCRIPTION_MAX_LENGTH, {
      message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_DESCRIPTION_TOO_LONG,
    }),
    groupType: optionGroupTypeSchema,
    required: z.boolean(),
    multipleSelect: z.boolean(),
    minSelect: z.string(),
    maxSelect: z.string(),
    /** 등록/수정 판별용 — `productId` 필수 여부를 가른다. 서버로는 보내지 않는다. */
    isCreate: z.boolean(),
  })
  .superRefine((values, ctx) => {
    if (values.isCreate && values.productId === null) {
      ctx.addIssue({
        code: "custom",
        path: ["productId"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.LINK_PRODUCT_REQUIRED,
      });
    }

    const min = parseOptionalPrice(values.minSelect, ctx, "minSelect", { allowEmpty: true });
    const max = parseOptionalPrice(values.maxSelect, ctx, "maxSelect", { allowEmpty: true });

    if (min !== undefined && max !== undefined && min > max) {
      ctx.addIssue({
        code: "custom",
        path: ["minSelect"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.MIN_SELECT_EXCEEDS_MAX,
      });
    }

    // 보증금 그룹의 필수 여부·선택 개수는 **서버가 강제**한다(`PRODUCT_OPTION_GROUP_DEPOSIT_*`).
    // 폼도 같은 값으로 고정해 보내지만, 폼을 거치지 않은 호출을 막기 위해 여기서 한 번 더 본다.
    if (values.groupType !== "CUP_DEPOSIT") return;

    if (values.required) {
      ctx.addIssue({
        code: "custom",
        path: ["required"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.DEPOSIT_GROUP_CANNOT_BE_REQUIRED,
      });
    }

    if (values.minSelect !== CUP_DEPOSIT_FIXED_MIN_SELECT || values.maxSelect !== CUP_DEPOSIT_FIXED_MAX_SELECT) {
      ctx.addIssue({
        code: "custom",
        path: ["maxSelect"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.DEPOSIT_GROUP_SELECT_FIXED,
      });
    }
  });

export type OptionGroupFormValues = z.infer<typeof optionGroupFormSchema>;

/**
 * 옵션 추가·수정 폼.
 *
 * 소속 옵션그룹의 유형에 따라 요구 필드가 갈리므로 `groupType` 을 폼 값으로 들고 다닌다
 * (사용자가 고르는 값이 아니라 다이얼로그가 주입하는 컨텍스트다).
 *
 * - `CUP_DEPOSIT` + 개인컵 아님 → `cupCount` 필수(`1~10`), `additionalPrice` 는 `0`
 * - `CUP_DEPOSIT` + 개인컵 → `personalCupDiscountAmount` 필수(`0` 이상), `cupCount` 비움
 * - `NORMAL` → `cupCount`·`personalCupDiscountAmount` 모두 비어야 한다(서버가 거부한다)
 */
export const optionFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_NAME_REQUIRED })
      .max(OPTION_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_NAME_TOO_LONG }),
    additionalPrice: z.string(),
    groupType: optionGroupTypeSchema,
    personalCup: z.boolean(),
    cupCount: z.string(),
    personalCupDiscountAmount: z.string(),
  })
  .superRefine((values, ctx) => {
    const additionalPrice = parseOptionalPrice(values.additionalPrice, ctx, "additionalPrice", { allowEmpty: true });
    const isDepositGroup = values.groupType === "CUP_DEPOSIT";

    if (!isDepositGroup) {
      if (values.personalCup) {
        ctx.addIssue({
          code: "custom",
          path: ["personalCup"],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP,
        });
      }
      if (values.cupCount.trim() !== "") {
        ctx.addIssue({
          code: "custom",
          path: ["cupCount"],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.CUP_COUNT_NOT_ALLOWED,
        });
      }
      if (values.personalCupDiscountAmount.trim() !== "") {
        ctx.addIssue({
          code: "custom",
          path: ["personalCupDiscountAmount"],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP,
        });
      }
      return;
    }

    // 보증금과 추가금을 섞으면 비과세 분리가 무너지므로 서버가 0 만 받는다.
    if (additionalPrice !== undefined && additionalPrice !== 0) {
      ctx.addIssue({
        code: "custom",
        path: ["additionalPrice"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.DEPOSIT_ADDITIONAL_PRICE_NOT_ALLOWED,
      });
    }

    if (values.personalCup) {
      if (values.cupCount.trim() !== "") {
        ctx.addIssue({
          code: "custom",
          path: ["cupCount"],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.CUP_COUNT_NOT_ALLOWED,
        });
      }

      const trimmedDiscount = values.personalCupDiscountAmount.trim();
      if (trimmedDiscount === "") {
        ctx.addIssue({
          code: "custom",
          path: ["personalCupDiscountAmount"],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.PERSONAL_CUP_DISCOUNT_REQUIRED,
        });
        return;
      }

      parseOptionalPrice(trimmedDiscount, ctx, "personalCupDiscountAmount", { allowEmpty: false });
      return;
    }

    // 값이 **들어 있는** 상황이므로 "입력해 주세요" 가 아니라 "설정할 수 없습니다" 가 맞다.
    // 개인컵 스위치를 껐다 켰다 하면 도달하는 경로라 문구가 뒤집히면 이해할 수 없는 에러가 된다.
    if (values.personalCupDiscountAmount.trim() !== "") {
      ctx.addIssue({
        code: "custom",
        path: ["personalCupDiscountAmount"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.PERSONAL_CUP_DISCOUNT_NOT_ALLOWED,
      });
    }

    const trimmedCupCount = values.cupCount.trim();
    if (trimmedCupCount === "") {
      ctx.addIssue({
        code: "custom",
        path: ["cupCount"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.CUP_COUNT_REQUIRED,
      });
      return;
    }

    const cupCount = Number(trimmedCupCount);
    if (!Number.isInteger(cupCount) || cupCount < CUP_COUNT_MIN || cupCount > CUP_COUNT_MAX) {
      ctx.addIssue({
        code: "custom",
        path: ["cupCount"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.CUP_COUNT_RANGE,
      });
    }
  });

export type OptionFormValues = z.infer<typeof optionFormSchema>;

/** 채식 설정 폼 */
export const vegetarianFormSchema = z.object({
  vegetarianType: z.string().min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.VEGETARIAN_TYPE_REQUIRED }),
  ingredients: z
    .string()
    .trim()
    .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.VEGETARIAN_INGREDIENTS_REQUIRED })
    .max(VEGETARIAN_INGREDIENTS_MAX_LENGTH, {
      message: PRODUCT_MENU_VALIDATION_MESSAGE.VEGETARIAN_INGREDIENTS_TOO_LONG,
    }),
  description: z.string().max(VEGETARIAN_DESCRIPTION_MAX_LENGTH, {
    message: PRODUCT_MENU_VALIDATION_MESSAGE.VEGETARIAN_DESCRIPTION_TOO_LONG,
  }),
});

export type VegetarianFormValues = z.infer<typeof vegetarianFormSchema>;

// ===== 서버 액션 입력 검증 =====
//
// 폼을 거치지 않고도 액션이 호출될 수 있으므로 액션 경계에서 한 번 더 본다.

export const shopIdSchema = z.number().int().positive({ message: PRODUCT_VALIDATION_MESSAGE.SHOP_REQUIRED });
export const productIdSchema = z.number().int().positive();

/** 순서 있는 id 배열. 서버가 인덱스로 `0..N-1` 정규화하므로 sort 값은 보내지 않는다 */
export const orderedIdsSchema = z.array(z.number().int().positive());

/** `yyyy-MM-dd`. 비어 있으면 무기한이라 null 을 허용한다 */
export const localDateSchema = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/)
  .nullable();

export const exposureDayTypeSchema = z.enum([
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
]);

const exposureTimeSchema = z
  .string()
  .regex(/^\d{2}:\d{2}(:\d{2})?$/)
  .nullable();

/**
 * 노출기간 저장 요청.
 *
 * **묶음과 개별 요일 혼용을 여기서 막는다.** 서버도 `PRODUCT_EXPOSURE_DAY_TYPE_MIXED` 로 막지만,
 * UI 가 라디오로 방식을 고르게 하는 이상 혼용 요청이 올라오는 것 자체가 버그다.
 */
export const exposureSaveSchema = z
  .object({
    startDate: localDateSchema,
    endDate: localDateSchema,
    hours: z.array(
      z.object({
        dayType: exposureDayTypeSchema,
        startTime: exposureTimeSchema,
        endTime: exposureTimeSchema,
      }),
    ),
  })
  .superRefine((values, ctx) => {
    if (values.startDate !== null && values.endDate !== null && values.endDate < values.startDate) {
      ctx.addIssue({
        code: "custom",
        path: ["endDate"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.EXPOSURE_PERIOD_INVALID,
      });
    }

    const hasPreset = values.hours.some((hour) => EXPOSURE_PRESET_DAY_TYPES.includes(hour.dayType));
    const hasIndividual = values.hours.some((hour) => !EXPOSURE_PRESET_DAY_TYPES.includes(hour.dayType));
    if (hasPreset && hasIndividual) {
      ctx.addIssue({
        code: "custom",
        path: ["hours"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.EXPOSURE_DAY_TYPE_MIXED,
      });
    }

    // 시작만 있고 종료가 없으면(또는 그 반대) 서버가 해석할 수 없다 — 종일은 둘 다 null 이다.
    for (const [index, hour] of values.hours.entries()) {
      if ((hour.startTime === null) !== (hour.endTime === null)) {
        ctx.addIssue({
          code: "custom",
          path: ["hours", index],
          message: PRODUCT_MENU_VALIDATION_MESSAGE.EXPOSURE_TIME_INCOMPLETE,
        });
      }
    }
  });

export const vegetarianTypeSchema = z.enum(["VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"]);

// ===== 옵션그룹 합치기 (`frontend.md` §2-9) =====

/**
 * 추천 제외 요청.
 *
 * `signature` 는 서버가 발급한 불투명 토큰이라 형식을 검사하지 않고 비어 있는지만 본다 —
 * 구조를 가정하면 서버가 서명 방식을 바꿀 때 프론트가 조용히 막는다.
 */
export const optionGroupMergeExclusionSchema = z.object({
  signature: z.string().min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.MERGE_SIGNATURE_REQUIRED }),
  optionGroupIds: z
    .array(z.number().int().positive())
    .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.MERGE_TARGET_REQUIRED }),
});

export type OptionGroupMergeExclusionValues = z.infer<typeof optionGroupMergeExclusionSchema>;

export const optionGroupMergeEntryTypeSchema = z.enum(["RECOMMENDED", "MANUAL"]);

/**
 * 합치기 실행 요청.
 *
 * 기준 그룹이 흡수 대상에 섞여 들어가면 서버가 `PRODUCT_OPTION_GROUP_MERGE_BASE_INCLUDED` 로
 * 거부하지만, 그 상태는 UI 버그이므로 여기서 먼저 막는다.
 */
export const optionGroupMergeSchema = z
  .object({
    baseOptionGroupId: z.number().int().positive(),
    optionGroupIds: z
      .array(z.number().int().positive())
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.MERGE_TARGET_REQUIRED }),
    entryType: optionGroupMergeEntryTypeSchema,
  })
  .superRefine((values, ctx) => {
    if (values.optionGroupIds.includes(values.baseOptionGroupId)) {
      ctx.addIssue({
        code: "custom",
        path: ["optionGroupIds"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.MERGE_BASE_INCLUDED,
      });
    }
  });

export type OptionGroupMergeValues = z.infer<typeof optionGroupMergeSchema>;
