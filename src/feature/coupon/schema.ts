import { z } from "zod";

export const COUPON_NAME_MAX = 200;
export const COUPON_DESC_MAX = 500;

export const RATE_DISCOUNT_MAX = 100;

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

// 미지정(무제한) 허용 숫자. 폼에서 빈 입력은 undefined 로 넘긴다.
const optionalNonNegativeInt = z.number().int().min(0, { message: "0 이상의 값을 입력해 주세요." }).optional();

/**
 * datetime-local 값("...THH:mm" 또는 "...THH:mm:ss")을 초 단위로 통일해 비교 가능한 시각으로 변환한다.
 * 문자열 사전순 비교는 두 값의 초 자리수가 다르면 어긋날 수 있어 Date 로 정규화한다.
 */
const toComparableTime = (value: string) => new Date(value.length === 16 ? `${value}:00` : value).getTime();

export const couponFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: "쿠폰 이름을 입력해 주세요." })
      .max(COUPON_NAME_MAX, {
        message: `쿠폰 이름은 최대 ${COUPON_NAME_MAX}자까지 입력할 수 있습니다.`,
      }),
    description: z
      .string()
      .max(COUPON_DESC_MAX, {
        message: `설명은 최대 ${COUPON_DESC_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional(),
    discountType: z.enum(["AMOUNT", "RATE"], { message: "할인 유형을 선택해 주세요." }),
    discountAmount: z
      .number({ message: "할인 값을 입력해 주세요." })
      .int()
      .min(1, { message: "할인 값은 1 이상이어야 합니다." }),
    maxDiscountAmount: optionalNonNegativeInt,
    minOrderAmount: z
      .number({ message: "최소 주문 금액을 입력해 주세요." })
      .int()
      .min(0, { message: "최소 주문 금액은 0 이상이어야 합니다." }),
    maxDiscountCount: optionalNonNegativeInt,
    issueStartAt: z.string().trim().min(1, { message: "발급 시작 일시를 입력해 주세요." }),
    issueEndAt: z.string().trim().min(1, { message: "발급 종료 일시를 입력해 주세요." }),
    useStartAt: z.string().trim().min(1, { message: "사용 시작 일시를 입력해 주세요." }),
    useEndAt: z.string().trim().min(1, { message: "사용 종료 일시를 입력해 주세요." }),
    visible: z.boolean(),
  })
  .refine((data) => toComparableTime(data.issueStartAt) <= toComparableTime(data.issueEndAt), {
    message: "발급 시작 일시는 종료 일시보다 이후일 수 없습니다.",
    path: ["issueEndAt"],
  })
  .refine((data) => toComparableTime(data.useStartAt) <= toComparableTime(data.useEndAt), {
    message: "사용 시작 일시는 종료 일시보다 이후일 수 없습니다.",
    path: ["useEndAt"],
  })
  .superRefine((data, ctx) => {
    // 정률(RATE)은 할인율이므로 100%를 초과할 수 없다.
    if (data.discountType === "RATE" && data.discountAmount > RATE_DISCOUNT_MAX) {
      ctx.addIssue({
        code: "custom",
        message: `정률 할인은 ${RATE_DISCOUNT_MAX}% 를 초과할 수 없습니다.`,
        path: ["discountAmount"],
      });
    }
    // 최대 할인 금액은 정률(RATE) 상한 개념이므로 정액(AMOUNT)에는 지정할 수 없다.
    if (data.discountType === "AMOUNT" && data.maxDiscountAmount !== undefined) {
      ctx.addIssue({
        code: "custom",
        message: "정액 할인에는 최대 할인 금액을 지정할 수 없습니다.",
        path: ["maxDiscountAmount"],
      });
    }
  });

export type CouponFormValues = z.infer<typeof couponFormSchema>;

export const couponIssueSchema = z.object({
  memberId: z.number({ message: "회원 ID를 입력해 주세요." }).int().positive({ message: "회원 ID는 양수여야 합니다." }),
});

export type CouponIssueFormValues = z.infer<typeof couponIssueSchema>;
