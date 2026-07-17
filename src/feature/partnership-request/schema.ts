import { z } from "zod";

export const partnershipStatusUpdateSchema = z.object({
  status: z.enum(["PENDING", "IN_PROGRESS", "COMPLETED"], {
    message: "변경할 처리 상태를 선택해 주세요.",
  }),
});

export type PartnershipStatusUpdateValues = z.infer<typeof partnershipStatusUpdateSchema>;
