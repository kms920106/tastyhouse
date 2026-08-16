import "server-only";

import type { ApiResponse } from "@/api/shared/types";
import type { CeoReplyPhrase } from "@/feature/ceo-reply-phrase/domain";

import type { CeoReplyPhraseResponse } from "./ceo-reply-phrase.dto";
import { ceoReplyPhraseRepository } from "./ceo-reply-phrase.repository";

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toPhrase(item: CeoReplyPhraseResponse): CeoReplyPhrase {
  return {
    id: item.id,
    name: item.name,
    displayName: item.displayName,
    content: item.content,
    sort: item.sort,
    createdAt: item.createdAt,
  };
}

export const ceoReplyPhraseService = {
  async getPhrases(): Promise<ApiResponse<CeoReplyPhrase[]>> {
    const result = await ceoReplyPhraseRepository.getPhrases();
    if (result.error || !result.data) return { ...result, data: undefined };
    return { ...result, data: result.data.map(toPhrase) };
  },
};
