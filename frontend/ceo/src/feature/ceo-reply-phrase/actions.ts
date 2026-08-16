"use server";

import { revalidatePath } from "next/cache";

import { ceoReplyPhraseRepository } from "@/api/ceo-reply-phrase/ceo-reply-phrase.repository";
import { ceoReplyPhraseService } from "@/api/ceo-reply-phrase/ceo-reply-phrase.service";

import type { CeoReplyPhrase } from "./domain";
import { CEO_REPLY_PHRASE_COPY, CEO_REPLY_PHRASE_ERROR_MESSAGE, CEO_REPLY_PHRASE_MESSAGE } from "./message";
import { type ReplyPhraseFormValues, replyPhraseIdSchema, replyPhraseSchema } from "./schema";

/** 문구는 리뷰 화면에서만 소비되므로 그 경로만 갱신한다. */
const SHOP_REVIEW_PATH = "/dashboard/shop/reviews";

type ActionResult<T = never> = {
  success: boolean;
  message?: string;
  data?: T;
};

/** 서버 실패를 사용자 문구로 바꾼다. */
function toFailure(errorCode: string | undefined, fallback: string): ActionResult<never> {
  return {
    success: false,
    message: (errorCode && CEO_REPLY_PHRASE_ERROR_MESSAGE[errorCode]) ?? fallback,
  };
}

function invalidInput(message?: string): ActionResult<never> {
  return { success: false, message: message ?? CEO_REPLY_PHRASE_MESSAGE.INVALID_INPUT };
}

/** 이름은 선택 항목이라 비어 있으면 아예 보내지 않는다 — 서버가 표시명을 내용에서 파생한다. */
function toUpsertBody(values: ReplyPhraseFormValues) {
  const name = values.name?.trim();
  return { content: values.content, ...(name ? { name } : {}) };
}

export async function fetchReplyPhrasesAction(): Promise<ActionResult<CeoReplyPhrase[]>> {
  const { data, error, errorCode } = await ceoReplyPhraseService.getPhrases();
  if (error !== undefined) return toFailure(errorCode, CEO_REPLY_PHRASE_COPY.LOAD_FAILED);

  return { success: true, data: data ?? [] };
}

export async function createReplyPhraseAction(values: ReplyPhraseFormValues): Promise<ActionResult<number>> {
  const parsed = replyPhraseSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error, errorCode } = await ceoReplyPhraseRepository.createPhrase(toUpsertBody(parsed.data));
  if (error !== undefined) return toFailure(errorCode, CEO_REPLY_PHRASE_COPY.CREATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true, data };
}

export async function updateReplyPhraseAction(id: number, values: ReplyPhraseFormValues): Promise<ActionResult<never>> {
  const parsedId = replyPhraseIdSchema.safeParse(id);
  if (!parsedId.success) return invalidInput();

  const parsed = replyPhraseSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, errorCode } = await ceoReplyPhraseRepository.updatePhrase(id, toUpsertBody(parsed.data));
  if (error !== undefined) return toFailure(errorCode, CEO_REPLY_PHRASE_COPY.UPDATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

export async function deleteReplyPhraseAction(id: number): Promise<ActionResult<never>> {
  const parsedId = replyPhraseIdSchema.safeParse(id);
  if (!parsedId.success) return invalidInput();

  const { error, errorCode } = await ceoReplyPhraseRepository.deletePhrase(id);
  if (error !== undefined) return toFailure(errorCode, CEO_REPLY_PHRASE_COPY.DELETE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}
