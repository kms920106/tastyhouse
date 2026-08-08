import { z } from "zod";

import {
  BUSINESS_HOUR_MAX_MINUTES,
  BUSINESS_HOUR_MIN_MINUTES,
  CLOSED_DAY_TYPE_OPTIONS,
  CONTENT_BOARD_DESCRIPTION_MAX,
  CONTENT_BOARD_MAX_COUNT,
  CONTENT_BOARD_TOPIC_OPTIONS,
  CONTENT_BOARD_TYPE_OPTIONS,
  DAY_TYPE_OPTIONS,
  DELIVERY_TIP_BASE_DISTANCE_OPTIONS,
  DELIVERY_TIP_EXTRA_UPPER_BOUND,
  DELIVERY_TIP_SCHEDULE_DISALLOWED_DAY_TYPES,
  DELIVERY_TIP_SURCHARGE_RULES,
  DELIVERY_TIP_SURCHARGE_UNIT_LABEL,
  DELIVERY_TIP_TIER_MAX_COUNT,
  DELIVERY_TIP_UNSET,
  DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE,
  MIN_ORDER_AMOUNT_LOWER_BOUND,
  MIN_ORDER_AMOUNT_UNSET,
  MIN_ORDER_AMOUNT_UPPER_BOUND,
  ORDER_METHOD_OPTIONS,
  SHOP_DIRECTIONS_MAX,
  SHOP_INTRODUCTION_MAX,
  SHOP_STATUS_OPTIONS,
  SUSPENSION_REASON_OPTIONS,
  TEMPORARY_CLOSURE_MAX_DAYS,
  TIME_STEP_MINUTES,
  WEEKDAY_OPTIONS,
} from "./constants";
import {
  countInclusiveDays,
  getDurationMinutes,
  isRangeOverlapping,
  isRangeWithin,
  isSameRange,
  isTimeStepValid,
  isValidCalendarDate,
  parseTimeToMinutes,
} from "./time";

/** 5분 단위 "HH:mm[:ss]" 시간 문자열 */
const timeString = z.string().refine(isTimeStepValid, {
  message: `시간은 ${TIME_STEP_MINUTES}분 단위로 입력해 주세요.`,
});

const dateString = z
  .string()
  .regex(/^\d{4}-\d{2}-\d{2}$/, { message: "날짜를 선택해 주세요." })
  .refine(isValidCalendarDate, { message: "존재하지 않는 날짜입니다." });

const dateTimeString = z
  .string()
  .min(1, { message: "일시를 입력해 주세요." })
  .refine((value) => !Number.isNaN(new Date(value).getTime()), { message: "일시 형식이 올바르지 않습니다." });

// ===== 기본정보 =====

export const shopIntroductionSchema = z.object({
  message: z
    .string()
    .trim()
    .max(SHOP_INTRODUCTION_MAX, {
      message: `가게 소개는 최대 ${SHOP_INTRODUCTION_MAX}자까지 입력할 수 있습니다.`,
    }),
});
export type ShopIntroductionFormValues = z.infer<typeof shopIntroductionSchema>;

export const shopStatusSchema = z.object({
  status: z.enum(SHOP_STATUS_OPTIONS),
});
export type ShopStatusFormValues = z.infer<typeof shopStatusSchema>;

// 최소주문금액은 0(미설정, 제한 없음) 또는 5,000~30,000원만 허용한다 — 서버의 도메인 불변식과 동일한 규칙.
const MIN_ORDER_AMOUNT_RANGE_MESSAGE =
  `최소주문금액은 ${MIN_ORDER_AMOUNT_LOWER_BOUND.toLocaleString("ko-KR")}원 이상 ` +
  `${MIN_ORDER_AMOUNT_UPPER_BOUND.toLocaleString("ko-KR")}원 이하로 입력해 주세요. ` +
  `설정하지 않으려면 0을 입력해 주세요.`;

export const shopMinOrderAmountSchema = z.object({
  minOrderAmount: z
    .number({ message: "최소주문금액을 입력해 주세요." })
    .int({ message: MIN_ORDER_AMOUNT_RANGE_MESSAGE })
    .superRefine((value, ctx) => {
      if (value === MIN_ORDER_AMOUNT_UNSET) return;
      if (value < MIN_ORDER_AMOUNT_LOWER_BOUND || value > MIN_ORDER_AMOUNT_UPPER_BOUND) {
        ctx.addIssue({ code: "custom", message: MIN_ORDER_AMOUNT_RANGE_MESSAGE });
      }
    }),
});
export type ShopMinOrderAmountFormValues = z.infer<typeof shopMinOrderAmountSchema>;

// ===== 예약주문 =====

export const shopScheduledOrderSchema = z.object({
  enabled: z.boolean(),
});
export type ShopScheduledOrderFormValues = z.infer<typeof shopScheduledOrderSchema>;

// ===== 배달팁 =====
// 서버 도메인 불변식을 그대로 미러링한다. 어긋나면 저장 시 SHOP_DELIVERY_TIP_* 에러로 거절된다.

const DELIVERY_TIP_RANGE_MESSAGE =
  `배달팁은 0원 이상 ${(DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE - 1).toLocaleString("ko-KR")}원 이하로 입력해 주세요. ` +
  `${DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE.toLocaleString("ko-KR")}원은 입력할 수 없습니다.`;

const EXTRA_DELIVERY_TIP_RANGE_MESSAGE = `추가 배달팁은 0원 이상 ${DELIVERY_TIP_EXTRA_UPPER_BOUND.toLocaleString("ko-KR")}원 이하로 입력해 주세요.`;

/** 기본 배달팁 금액 — 5,000원 미만만 허용(5,000원 자체 불가) */
const deliveryTipAmount = z
  .number({ message: "배달팁을 입력해 주세요." })
  .int({ message: DELIVERY_TIP_RANGE_MESSAGE })
  .min(DELIVERY_TIP_UNSET, { message: DELIVERY_TIP_RANGE_MESSAGE })
  .lt(DELIVERY_TIP_UPPER_BOUND_EXCLUSIVE, { message: DELIVERY_TIP_RANGE_MESSAGE });

/** 추가 배달팁 금액 — 10,000원 이하 허용 */
const extraDeliveryTipAmount = z
  .number({ message: "추가 배달팁을 입력해 주세요." })
  .int({ message: EXTRA_DELIVERY_TIP_RANGE_MESSAGE })
  .min(DELIVERY_TIP_UNSET, { message: EXTRA_DELIVERY_TIP_RANGE_MESSAGE })
  .max(DELIVERY_TIP_EXTRA_UPPER_BOUND, { message: EXTRA_DELIVERY_TIP_RANGE_MESSAGE });

const deliveryTipTierSchema = z.object({
  minOrderAmount: z
    .number({ message: "주문금액을 입력해 주세요." })
    .int({ message: "주문금액은 0원 이상으로 입력해 주세요." })
    .min(0, { message: "주문금액은 0원 이상으로 입력해 주세요." }),
  tipAmount: deliveryTipAmount,
});

/**
 * 기본 배달팁 구간 — 최대 3구간.
 * 주문금액은 strict 오름차순, 배달팁은 strict 내림차순이어야 한다
 * (주문금액이 높아질수록 배달팁이 낮아져야 한다는 PDF 규칙).
 */
export const deliveryTipTiersSchema = z.object({
  tiers: z
    .array(deliveryTipTierSchema)
    .min(1, { message: "배달팁 구간을 최소 1개 입력해 주세요." })
    .max(DELIVERY_TIP_TIER_MAX_COUNT, {
      message: `배달팁 구간은 최대 ${DELIVERY_TIP_TIER_MAX_COUNT}개까지 설정할 수 있습니다.`,
    })
    .superRefine((tiers, ctx) => {
      tiers.forEach((tier, index) => {
        if (index === 0) return;
        const previous = tiers[index - 1];

        if (tier.minOrderAmount <= previous.minOrderAmount) {
          ctx.addIssue({
            code: "custom",
            path: [index, "minOrderAmount"],
            message: "주문금액은 앞 구간보다 커야 합니다.",
          });
        }
        if (tier.tipAmount >= previous.tipAmount) {
          ctx.addIssue({
            code: "custom",
            path: [index, "tipAmount"],
            message: "주문금액이 높은 구간의 배달팁은 앞 구간보다 낮아야 합니다.",
          });
        }
      });
    }),
});
export type DeliveryTipTiersFormValues = z.infer<typeof deliveryTipTiersSchema>;

/**
 * 거리별 추가 배달팁.
 * 할증 금액의 허용 범위가 단위에 따라 달라진다 — 100m당 100~300원, 500m당 100~1,500원.
 */
export const deliveryTipDistanceSchema = z
  .object({
    baseDistanceMeters: z.union(
      DELIVERY_TIP_BASE_DISTANCE_OPTIONS.map((option) => z.literal(option)) as [
        z.ZodLiteral<number>,
        z.ZodLiteral<number>,
        ...z.ZodLiteral<number>[],
      ],
      { message: "기본배달거리를 선택해 주세요." },
    ),
    surchargeUnit: z.enum(["PER_100M", "PER_500M"], { message: "할증 단위를 선택해 주세요." }),
    surchargeAmount: z.number({ message: "할증 금액을 입력해 주세요." }).int({ message: "할증 금액을 입력해 주세요." }),
  })
  .superRefine((values, ctx) => {
    const rule = DELIVERY_TIP_SURCHARGE_RULES[values.surchargeUnit];
    if (values.surchargeAmount < rule.min || values.surchargeAmount > rule.max) {
      ctx.addIssue({
        code: "custom",
        path: ["surchargeAmount"],
        message:
          `${DELIVERY_TIP_SURCHARGE_UNIT_LABEL[values.surchargeUnit]} 할증 금액은 ` +
          `${rule.min.toLocaleString("ko-KR")}원 이상 ${rule.max.toLocaleString("ko-KR")}원 이하로 입력해 주세요.`,
      });
    }
  });
export type DeliveryTipDistanceFormValues = z.infer<typeof deliveryTipDistanceSchema>;

/** 지역별 추가 배달팁 — 같은 행정동을 중복 선택할 수 없다 */
export const deliveryTipRegionsSchema = z.object({
  regions: z
    .array(
      z.object({
        adminDongId: z
          .number({ message: "지역을 선택해 주세요." })
          .int()
          .positive({ message: "지역을 선택해 주세요." }),
        tipAmount: extraDeliveryTipAmount,
      }),
    )
    .min(1, { message: "지역을 최소 1개 선택해 주세요." })
    .superRefine((regions, ctx) => {
      const seen = new Map<number, number>();
      regions.forEach((region, index) => {
        const firstIndex = seen.get(region.adminDongId);
        if (firstIndex !== undefined) {
          ctx.addIssue({
            code: "custom",
            path: [index, "adminDongId"],
            message: "이미 선택한 지역입니다.",
          });
          return;
        }
        seen.set(region.adminDongId, index);
      });
    }),
});
export type DeliveryTipRegionsFormValues = z.infer<typeof deliveryTipRegionsSchema>;

/**
 * 시간별 추가 배달팁.
 * 일요일·공휴일은 시간별에서 사용할 수 없고(서버가 거부), 같은 요일 구분 안에서 시간대가 겹칠 수 없다.
 * 자정을 넘기는 구간도 겹침 판정 대상이다.
 */
export const deliveryTipSchedulesSchema = z.object({
  schedules: z
    .array(
      z.object({
        dayType: z
          .enum(DAY_TYPE_OPTIONS, { message: "요일을 선택해 주세요." })
          .refine((dayType) => !(DELIVERY_TIP_SCHEDULE_DISALLOWED_DAY_TYPES as readonly string[]).includes(dayType), {
            message: "선택할 수 없는 요일 구분입니다. 공휴일은 공휴일 배달팁으로 설정해 주세요.",
          }),
        startTime: timeString,
        endTime: timeString,
        tipAmount: extraDeliveryTipAmount,
      }),
    )
    .min(1, { message: "시간대를 최소 1개 입력해 주세요." })
    .superRefine((schedules, ctx) => {
      schedules.forEach((schedule, index) => {
        if (parseTimeToMinutes(schedule.startTime) === parseTimeToMinutes(schedule.endTime)) {
          ctx.addIssue({
            code: "custom",
            path: [index, "endTime"],
            message: "시작 시간과 종료 시간이 같을 수 없습니다.",
          });
        }
      });

      // 같은 요일 구분끼리만 겹침을 판정한다 (평일/주말 같은 묶음과 개별 요일의 교차는 서버가 판정)
      schedules.forEach((schedule, index) => {
        for (let other = 0; other < index; other += 1) {
          const previous = schedules[other];
          if (previous.dayType !== schedule.dayType) continue;
          if (isRangeOverlapping(previous.startTime, previous.endTime, schedule.startTime, schedule.endTime)) {
            ctx.addIssue({
              code: "custom",
              path: [index, "startTime"],
              message: "같은 요일에 이미 설정한 시간대와 겹칩니다.",
            });
            return;
          }
        }
      });
    }),
});
export type DeliveryTipSchedulesFormValues = z.infer<typeof deliveryTipSchedulesSchema>;

/** 공휴일 추가 배달팁 — 0이면 해제 */
export const deliveryTipHolidaySchema = z.object({
  tipAmount: extraDeliveryTipAmount,
});
export type DeliveryTipHolidayFormValues = z.infer<typeof deliveryTipHolidaySchema>;

export const convenienceInfoSchema = z.object({
  parkingAvailable: z.boolean(),
  parkingPaid: z.boolean(),
  valetAvailable: z.boolean(),
  valetPaid: z.boolean(),
  directionsGuide: z
    .string()
    .trim()
    .max(SHOP_DIRECTIONS_MAX, {
      message: `찾아오는 길 안내는 최대 ${SHOP_DIRECTIONS_MAX}자까지 입력할 수 있습니다.`,
    }),
  displayLatitude: z
    .number({ message: "위도를 입력해 주세요." })
    .min(-90, { message: "위도는 -90 ~ 90 사이여야 합니다." })
    .max(90, { message: "위도는 -90 ~ 90 사이여야 합니다." }),
  displayLongitude: z
    .number({ message: "경도를 입력해 주세요." })
    .min(-180, { message: "경도는 -180 ~ 180 사이여야 합니다." })
    .max(180, { message: "경도는 -180 ~ 180 사이여야 합니다." }),
});
export type ConvenienceInfoFormValues = z.infer<typeof convenienceInfoSchema>;

// 가상번호 허용 접두사 — 서울(02) / 지역번호(031~064) / 인터넷전화(070) / 휴대폰(010)
const PHONE_PREFIX_PATTERN = /^(02|0(3[1-3]|4[1-4]|5[1-5]|6[1-4])|070|010)/;

export const phoneNumberSchema = z.object({
  phoneNumber: z
    .string()
    .trim()
    .regex(/^\d+$/, { message: "전화번호는 숫자만 입력해 주세요." })
    .min(8, { message: "전화번호는 8~13자리로 입력해 주세요." })
    .max(13, { message: "전화번호는 8~13자리로 입력해 주세요." })
    .regex(PHONE_PREFIX_PATTERN, { message: "사용할 수 없는 지역번호입니다." }),
  virtual: z.boolean(),
});
export type PhoneNumberFormValues = z.infer<typeof phoneNumberSchema>;

const YOUTUBE_URL_PATTERN = /^https:\/\/(www\.youtube\.com\/watch\?v=|youtu\.be\/|www\.youtube\.com\/shorts\/)[\w-]+/;

export const contentBoardSchema = z
  .object({
    contentType: z.enum(CONTENT_BOARD_TYPE_OPTIONS),
    topic: z.enum(CONTENT_BOARD_TOPIC_OPTIONS),
    youtubeUrl: z.string().trim().optional(),
    description: z
      .string()
      .trim()
      .max(CONTENT_BOARD_DESCRIPTION_MAX, {
        message: `설명은 최대 ${CONTENT_BOARD_DESCRIPTION_MAX}자까지 입력할 수 있습니다.`,
      }),
    hasExistingFile: z.boolean().optional(),
  })
  .superRefine((values, ctx) => {
    // 동영상은 YouTube URL 만 허용하고 파일 업로드를 받지 않는다.
    if (values.contentType === "VIDEO") {
      if (!values.youtubeUrl) {
        ctx.addIssue({ code: "custom", path: ["youtubeUrl"], message: "YouTube 주소를 입력해 주세요." });
        return;
      }
      if (!YOUTUBE_URL_PATTERN.test(values.youtubeUrl)) {
        ctx.addIssue({ code: "custom", path: ["youtubeUrl"], message: "YouTube 주소만 등록할 수 있습니다." });
      }
      return;
    }

    if (!values.hasExistingFile) {
      ctx.addIssue({ code: "custom", path: ["contentType"], message: "이미지를 첨부해 주세요." });
    }
  });
export type ContentBoardFormValues = z.infer<typeof contentBoardSchema>;

export const CONTENT_BOARD_LIMIT_MESSAGE = `콘텐츠보드는 최대 ${CONTENT_BOARD_MAX_COUNT}건까지 등록할 수 있습니다.`;

// ===== 운영정보 =====

/**
 * 요일 하나에 대한 영업시간 편집 값.
 * is24Hours 가 켜지면 시간 입력을 검증하지 않고, 휴게시간은 영업시간 범위 안에 있어야 한다.
 */
export const businessHourFormSchema = z
  .object({
    dayType: z.enum(DAY_TYPE_OPTIONS),
    isClosed: z.boolean(),
    is24Hours: z.boolean(),
    openTime: timeString,
    closeTime: timeString,
    breakTimeEnabled: z.boolean(),
    breakTime: z.object({
      startTime: timeString,
      endTime: timeString,
    }),
    applyToDays: z.array(z.enum(WEEKDAY_OPTIONS)),
  })
  .superRefine((values, ctx) => {
    if (values.isClosed || values.is24Hours) return;

    const duration = getDurationMinutes(values.openTime, values.closeTime);
    if (duration === null || duration < BUSINESS_HOUR_MIN_MINUTES) {
      ctx.addIssue({
        code: "custom",
        path: ["closeTime"],
        message: "영업시간은 최소 1시간 이상이어야 합니다.",
      });
      return;
    }
    if (duration > BUSINESS_HOUR_MAX_MINUTES) {
      ctx.addIssue({
        code: "custom",
        path: ["closeTime"],
        message: "영업시간은 최대 23시간 55분까지 설정할 수 있습니다. 24시간 영업을 사용해 주세요.",
      });
      return;
    }

    if (values.breakTimeEnabled) {
      const within = isRangeWithin(
        values.openTime,
        values.closeTime,
        values.breakTime.startTime,
        values.breakTime.endTime,
      );
      if (within === null) {
        ctx.addIssue({
          code: "custom",
          path: ["breakTime", "endTime"],
          message: "휴게시간 형식이 올바르지 않습니다.",
        });
      } else if (!within) {
        ctx.addIssue({
          code: "custom",
          path: ["breakTime", "endTime"],
          message: "휴게시간은 영업시간 범위 안에서만 설정할 수 있습니다.",
        });
      } else if (isSameRange(values.openTime, values.closeTime, values.breakTime.startTime, values.breakTime.endTime)) {
        ctx.addIssue({
          code: "custom",
          path: ["breakTime", "endTime"],
          message: "휴게시간을 영업시간과 동일하게 설정할 수 없습니다.",
        });
      }
    }
  });
export type BusinessHourFormValues = z.infer<typeof businessHourFormSchema>;

export const businessHourSchema = z.object({
  dayType: z.enum(DAY_TYPE_OPTIONS),
  openTime: timeString,
  closeTime: timeString,
  isClosed: z.boolean(),
  is24Hours: z.boolean(),
});
export type BusinessHourValues = z.infer<typeof businessHourSchema>;

export const dayTimeRangeSchema = z.object({
  dayType: z.enum(DAY_TYPE_OPTIONS),
  startTime: timeString,
  endTime: timeString,
});
export type DayTimeRangeValues = z.infer<typeof dayTimeRangeSchema>;

export const closedDaySchema = z.object({
  closedDayType: z.enum(CLOSED_DAY_TYPE_OPTIONS),
});
export type ClosedDayFormValues = z.infer<typeof closedDaySchema>;

export const holidayClosedSchema = z.object({
  closedOnPublicHolidays: z.boolean(),
});
export type HolidayClosedFormValues = z.infer<typeof holidayClosedSchema>;

export const temporaryClosureSchema = z
  .object({
    startDate: dateString,
    endDate: dateString,
  })
  .superRefine((values, ctx) => {
    const days = countInclusiveDays(values.startDate, values.endDate);
    if (days === null || days < 1) {
      ctx.addIssue({ code: "custom", path: ["endDate"], message: "종료일은 시작일 이후여야 합니다." });
    }
  });
export type TemporaryClosureFormValues = z.infer<typeof temporaryClosureSchema>;

// 임시휴무는 단일 기간이 아니라 이미 등록된 기간과의 누적 합계로 30일을 검증한다(서버 검증).
// 클라이언트는 신규 요청 기간 자체의 형식/순서만 확인하고, 누적 한도는 서버 에러 메시지를 그대로 노출한다.
export const TEMPORARY_CLOSURE_LIMIT_MESSAGE = `임시휴무는 누적 최대 ${TEMPORARY_CLOSURE_MAX_DAYS}일까지 설정할 수 있습니다.`;

// ===== 영업임시중지 =====

export const suspensionSchema = z
  .object({
    shopIds: z.array(z.number().int().positive()).min(1, { message: "임시중지할 가게를 선택해 주세요." }),
    reason: z.enum(SUSPENSION_REASON_OPTIONS),
    orderMethods: z.array(z.enum(ORDER_METHOD_OPTIONS)),
    startAt: dateTimeString,
    endAt: dateTimeString,
  })
  .superRefine((values, ctx) => {
    if (new Date(values.endAt).getTime() <= new Date(values.startAt).getTime()) {
      ctx.addIssue({ code: "custom", path: ["endAt"], message: "종료 일시는 시작 일시 이후여야 합니다." });
    }
  });
export type SuspensionFormValues = z.infer<typeof suspensionSchema>;
