import { z } from "zod";

import { PHRASE_CONTENT_MAX_LENGTH, PHRASE_NAME_MAX_LENGTH } from "./constants";
import { CEO_REPLY_PHRASE_VALIDATION_MESSAGE } from "./message";

export const replyPhraseSchema = z.object({
  name: z
    .string()
    .trim()
    .max(PHRASE_NAME_MAX_LENGTH, { message: CEO_REPLY_PHRASE_VALIDATION_MESSAGE.NAME_MAX_LENGTH })
    .optional(),
  content: z
    .string()
    .trim()
    .min(1, { message: CEO_REPLY_PHRASE_VALIDATION_MESSAGE.CONTENT_REQUIRED })
    .max(PHRASE_CONTENT_MAX_LENGTH, {
      message: CEO_REPLY_PHRASE_VALIDATION_MESSAGE.CONTENT_MAX_LENGTH,
    }),
});

export type ReplyPhraseFormValues = z.infer<typeof replyPhraseSchema>;

/**
 * 문구 ID 검증.
 *
 * Server Action 은 클라이언트를 거치지 않고도 호출될 수 있으므로 식별자도 여기서 다시 태운다
 * (`shop-review/actions.ts` 와 같은 판단).
 */
export const replyPhraseIdSchema = z.number().int().positive();
