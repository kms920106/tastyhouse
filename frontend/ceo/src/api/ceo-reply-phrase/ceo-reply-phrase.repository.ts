import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type { CeoReplyPhraseResponse, CeoReplyPhraseUpsertRequest } from "./ceo-reply-phrase.dto";

/**
 * 자주 쓰는 문구 API (transport only)
 *
 * `shopId` 가 없는 **점주 계정 리소스**라 다른 리소스와 달리 `/api/shops/v1/{shopId}/...` 형태가
 * 아니다 — 한 점주가 여러 가게를 맡아도 문구를 공유한다(`docs/tasks/backend.md` 3-4).
 *
 * 5건 상한이라 목록에 페이징이 없다.
 */

const ENDPOINT = "/api/ceos";

const REPLY_PHRASE_PATH = `${ENDPOINT}/v1/reply-phrases`;

export const ceoReplyPhraseRepository = {
  getPhrases(): Promise<ApiResponse<CeoReplyPhraseResponse[]>> {
    return api.get<CeoReplyPhraseResponse[]>(REPLY_PHRASE_PATH);
  },

  // 등록 POST 는 생성된 PK 만 반환하는 규칙을 따른다.
  createPhrase(body: CeoReplyPhraseUpsertRequest): Promise<ApiResponse<number>> {
    return api.post<number>(REPLY_PHRASE_PATH, body);
  },

  updatePhrase(id: number, body: CeoReplyPhraseUpsertRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${REPLY_PHRASE_PATH}/${id}`, body);
  },

  deletePhrase(id: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${REPLY_PHRASE_PATH}/${id}`);
  },
};
