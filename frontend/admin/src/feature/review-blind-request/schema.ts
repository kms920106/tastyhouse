import { z } from "zod";

// 반려 사유 필수 — 점주에게 그대로 노출되므로 빈 문자열/공백만 입력을 막는다.
export const rejectSchema = z.object({
  rejectReason: z
    .string()
    .trim()
    .min(1, "반려 사유를 입력해주세요.")
    .max(500, "반려 사유는 500자 이내로 입력해주세요."),
});

export type RejectFormValues = z.infer<typeof rejectSchema>;
