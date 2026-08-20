import { z } from "zod";

import {
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

/** 옵션그룹 추가·수정 폼 */
export const optionGroupFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_NAME_REQUIRED })
      .max(OPTION_GROUP_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_NAME_TOO_LONG }),
    description: z.string().max(OPTION_GROUP_DESCRIPTION_MAX_LENGTH, {
      message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_GROUP_DESCRIPTION_TOO_LONG,
    }),
    required: z.boolean(),
    multipleSelect: z.boolean(),
    minSelect: z.string(),
    maxSelect: z.string(),
  })
  .superRefine((values, ctx) => {
    const min = parseOptionalPrice(values.minSelect, ctx, "minSelect", { allowEmpty: true });
    const max = parseOptionalPrice(values.maxSelect, ctx, "maxSelect", { allowEmpty: true });

    if (min !== undefined && max !== undefined && min > max) {
      ctx.addIssue({
        code: "custom",
        path: ["minSelect"],
        message: PRODUCT_MENU_VALIDATION_MESSAGE.MIN_SELECT_EXCEEDS_MAX,
      });
    }
  });

export type OptionGroupFormValues = z.infer<typeof optionGroupFormSchema>;

/** 옵션 추가·수정 폼 */
export const optionFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_NAME_REQUIRED })
      .max(OPTION_NAME_MAX_LENGTH, { message: PRODUCT_MENU_VALIDATION_MESSAGE.OPTION_NAME_TOO_LONG }),
    additionalPrice: z.string(),
  })
  .superRefine((values, ctx) => {
    parseOptionalPrice(values.additionalPrice, ctx, "additionalPrice", { allowEmpty: true });
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
