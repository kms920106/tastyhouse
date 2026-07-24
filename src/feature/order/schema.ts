import { z } from "zod";

export const orderStatusUpdateSchema = z.object({
  status: z.enum(["PENDING", "CONFIRMED", "PREPARING", "COMPLETED", "CANCELLED"], {
    message: "변경할 주문 상태를 선택해 주세요.",
  }),
});

export type OrderStatusUpdateValues = z.infer<typeof orderStatusUpdateSchema>;
