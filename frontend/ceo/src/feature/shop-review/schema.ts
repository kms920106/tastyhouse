import { z } from "zod";

import { BLIND_DETAIL_REASON_MAX_LENGTH, BLIND_REASON_ETC, OWNER_REPLY_MAX_LENGTH } from "./constants";
import { SHOP_REVIEW_VALIDATION_MESSAGE } from "./message";

/** 사장님 답변 등록·수정 (`docs/tasks/backend.md` 1-6·1-7) */
export const ownerReplySchema = z.object({
  content: z
    .string()
    .trim()
    .min(1, { message: SHOP_REVIEW_VALIDATION_MESSAGE.OWNER_REPLY_REQUIRED })
    .max(OWNER_REPLY_MAX_LENGTH, { message: SHOP_REVIEW_VALIDATION_MESSAGE.OWNER_REPLY_MAX_LENGTH }),
});

export type OwnerReplyFormValues = z.infer<typeof ownerReplySchema>;

/**
 * 게시중단 요청 (`docs/tasks/backend.md` 1-9).
 *
 * `reason` 은 서버 카탈로그에서 받은 코드지만, 사유별 분기(`ETC` 면 상세 필수)가 스키마에
 * 필요하므로 값 집합을 여기에서도 고정한다. 카탈로그에 사유가 추가되면 이 enum 도 함께 넓힌다.
 */
export const blindRequestSchema = z
  .object({
    reason: z.enum(["ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"], {
      message: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_REASON_REQUIRED,
    }),
    detailReason: z
      .string()
      .trim()
      .max(BLIND_DETAIL_REASON_MAX_LENGTH, {
        message: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_DETAIL_REASON_MAX_LENGTH,
      })
      .optional(),
  })
  // 기타 사유는 상세 내용이 없으면 관리자가 판단할 근거가 없다 — 서버도 같은 규칙으로 400 을 낸다.
  .superRefine((values, ctx) => {
    if (values.reason !== BLIND_REASON_ETC) return;
    if (values.detailReason && values.detailReason.length > 0) return;

    ctx.addIssue({
      code: "custom",
      path: ["detailReason"],
      message: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_DETAIL_REASON_REQUIRED,
    });
  });

export type BlindRequestFormValues = z.infer<typeof blindRequestSchema>;

/** 앱 노출 정렬 설정 (`docs/tasks/backend.md` 1-5) */
export const sortTypeSchema = z.object({
  sortType: z.enum(["RECOMMENDED", "LATEST", "OLDEST"], {
    message: SHOP_REVIEW_VALIDATION_MESSAGE.SORT_TYPE_REQUIRED,
  }),
});

export type SortTypeFormValues = z.infer<typeof sortTypeSchema>;
