import { z } from "zod";

import { REASON_DETAIL_MAX } from "./constants";

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

export const withdrawalFormSchema = z.object({
  reason: z.enum(
    ["LOW_USAGE_FREQUENCY", "INSUFFICIENT_CONTENT", "SWITCH_TO_ANOTHER_SERVICE", "PRIVACY_CONCERNS", "OTHER"],
    { message: "탈퇴 사유를 선택해 주세요." },
  ),
  reasonDetail: z
    .string()
    .max(REASON_DETAIL_MAX, {
      message: `사유 상세는 최대 ${REASON_DETAIL_MAX}자까지 입력할 수 있습니다.`,
    })
    .transform(emptyToUndefined)
    .optional(),
});

export type WithdrawalFormValues = z.infer<typeof withdrawalFormSchema>;
