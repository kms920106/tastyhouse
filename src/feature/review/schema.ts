import { z } from "zod";

export const reviewHiddenSchema = z.object({
  hidden: z.boolean(),
});

export type ReviewHiddenFormValues = z.infer<typeof reviewHiddenSchema>;
