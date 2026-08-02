import { z } from "zod";

import { BUG_CATEGORY, BUG_PRIORITY, BUG_STATUS_TRANSITIONS } from "./constants";

export const BUG_ANSWER_MAX = 1000;

// 처리 상태 변경 (RECEIVED 로의 되돌림 불가)
export const statusUpdateSchema = z.object({
  status: z.enum(BUG_STATUS_TRANSITIONS, { message: "처리 상태를 선택해 주세요." }),
  answer: z
    .string()
    .trim()
    .max(BUG_ANSWER_MAX, { message: `처리 결과는 최대 ${BUG_ANSWER_MAX}자까지 입력할 수 있습니다.` })
    .optional(),
});
export type StatusUpdateValues = z.infer<typeof statusUpdateSchema>;

// 분류/우선순위 지정
export const classifySchema = z.object({
  category: z.enum(BUG_CATEGORY, { message: "분류를 선택해 주세요." }),
  priority: z.enum(BUG_PRIORITY, { message: "우선순위를 선택해 주세요." }),
});
export type ClassifyValues = z.infer<typeof classifySchema>;

// 담당자 배정
export const assignSchema = z.object({
  assigneeAdminId: z
    .number({ message: "담당 관리자 ID를 입력해 주세요." })
    .int({ message: "담당 관리자 ID는 정수여야 합니다." })
    .positive({ message: "담당 관리자 ID는 1 이상이어야 합니다." }),
});
export type AssignValues = z.infer<typeof assignSchema>;
