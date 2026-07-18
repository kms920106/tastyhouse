import { z } from "zod";

import { RANK_TYPE_VALUES } from "@/api/rank/rank.dto";

import { PRIZE_BRAND_MAX, PRIZE_NAME_MAX } from "./constants";

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

/**
 * datetime-local input 값("YYYY-MM-DDTHH:mm")을 API LocalDateTime 문자열
 * ("YYYY-MM-DDTHH:mm:ss")로 변환한다. 이미 초가 포함되어 있으면 그대로 둔다.
 */
export function toApiDateTime(value: string): string {
  return /T\d{2}:\d{2}$/.test(value) ? `${value}:00` : value;
}

/**
 * API LocalDateTime 문자열("YYYY-MM-DDTHH:mm:ss")을 datetime-local input 값
 * ("YYYY-MM-DDTHH:mm")으로 절삭한다.
 */
export function toInputDateTime(value: string): string {
  return value.length > 16 ? value.slice(0, 16) : value;
}

export const periodSchema = z
  .object({
    startAt: z.string().min(1, { message: "시작 일시를 입력해 주세요." }),
    endAt: z.string().min(1, { message: "종료 일시를 입력해 주세요." }),
    visible: z.boolean(),
  })
  .superRefine((data, ctx) => {
    if (data.startAt && data.endAt && toApiDateTime(data.endAt) <= toApiDateTime(data.startAt)) {
      ctx.addIssue({
        code: "custom",
        message: "종료 일시는 시작 일시보다 이후여야 합니다.",
        path: ["endAt"],
      });
    }
  });

export type PeriodFormValues = z.infer<typeof periodSchema>;

export const prizeSchema = z.object({
  prizeRank: z.number({ message: "등수를 입력해 주세요." }).int().positive({ message: "등수는 1 이상이어야 합니다." }),
  name: z
    .string()
    .trim()
    .min(1, { message: "경품 이름을 입력해 주세요." })
    .max(PRIZE_NAME_MAX, { message: `경품 이름은 최대 ${PRIZE_NAME_MAX}자까지 입력할 수 있습니다.` }),
  brand: z
    .string()
    .trim()
    .min(1, { message: "브랜드를 입력해 주세요." })
    .max(PRIZE_BRAND_MAX, { message: `브랜드는 최대 ${PRIZE_BRAND_MAX}자까지 입력할 수 있습니다.` }),
  imageFileId: z.number().int().positive().optional(),
});

export type PrizeFormValues = z.infer<typeof prizeSchema>;

export const aggregationSchema = z.object({
  type: z.enum(RANK_TYPE_VALUES).optional(),
  baseDate: z.string().transform(emptyToUndefined).optional(),
  limit: z.number().int().positive({ message: "상위 N명은 1 이상이어야 합니다." }).optional(),
});

export type AggregationFormValues = z.infer<typeof aggregationSchema>;
