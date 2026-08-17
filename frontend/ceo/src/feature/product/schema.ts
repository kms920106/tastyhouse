import { z } from "zod";

import { MINUTE_OPTIONS, SOLD_OUT_UNTIL_MAX_DAYS, SOLD_OUT_UNTIL_MIN_MINUTES } from "./constants";
import { PRODUCT_MESSAGE, PRODUCT_VALIDATION_MESSAGE } from "./message";

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
