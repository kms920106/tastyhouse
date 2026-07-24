import { z } from "zod";

import { REASON_MAX } from "./constants";

// 폼 input(문자열)을 1 이상 정수로 검증 후 number로 변환. 빈 값/음수/소수/비숫자 방어.
const pointAmountSchema = z
  .string()
  .trim()
  .min(1, { message: "적립/차감 금액을 입력해 주세요." })
  .regex(/^[1-9]\d*$/, { message: "금액은 1 이상의 정수로 입력해 주세요." })
  .transform((value) => Number(value));

const reasonSchema = z
  .string()
  .trim()
  .min(1, { message: "사유를 입력해 주세요." })
  .max(REASON_MAX, { message: `사유는 최대 ${REASON_MAX}자까지 입력할 수 있습니다.` });

export const pointEarnFormSchema = z.object({
  amount: pointAmountSchema,
  reason: reasonSchema,
});

export const pointDeductFormSchema = z.object({
  amount: pointAmountSchema,
  reason: reasonSchema,
});

// 폼 입력값(coerce 이전): amount는 number Input 문자열
export type PointEarnFormInput = z.input<typeof pointEarnFormSchema>;
export type PointDeductFormInput = z.input<typeof pointDeductFormSchema>;

// 검증/coerce 이후 값 (Server Action 전달용)
export type PointEarnFormValues = z.output<typeof pointEarnFormSchema>;
export type PointDeductFormValues = z.output<typeof pointDeductFormSchema>;

// Server Action 재검증용: 클라이언트에서 이미 coerce된 값(amount: number)을 검증
const pointAmountValueSchema = z.number().int().min(1, { message: "적립/차감 금액을 입력해 주세요." });

export const pointEarnValuesSchema = z.object({
  amount: pointAmountValueSchema,
  reason: reasonSchema,
});

export const pointDeductValuesSchema = z.object({
  amount: pointAmountValueSchema,
  reason: reasonSchema,
});
