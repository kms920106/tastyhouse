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
  isRangeWithin,
  isSameRange,
  isTimeStepValid,
  isValidCalendarDate,
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
