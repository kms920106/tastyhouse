import { z } from "zod";

export const reviewHiddenSchema = z.object({
  hidden: z.boolean(),
});

export type ReviewHiddenFormValues = z.infer<typeof reviewHiddenSchema>;

/** 리뷰 목록 검색 조건. 미지정(undefined) 이면 해당 조건을 서버로 보내지 않는다. */
export const reviewSearchSchema = z.object({
  shopId: z.number().int().positive().optional(),
  productId: z.number().int().positive().optional(),
  memberId: z.number().int().positive().optional(),
  hidden: z.boolean().optional(),
  ownerOnly: z.boolean().optional(),
  content: z.string().optional(),
  minRating: z.number().min(0).max(5).optional(),
  maxRating: z.number().min(0).max(5).optional(),
});

export type ReviewSearchValues = z.infer<typeof reviewSearchSchema>;
