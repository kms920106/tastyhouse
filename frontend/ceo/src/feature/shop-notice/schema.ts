import { z } from "zod";

import { NOTICE_CONTENT_MAX } from "./constants";
import { NOTICE_VALIDATION_MESSAGE } from "./message";

/**
 * 공지 등록·수정 폼 스키마.
 *
 * 이미지는 여기에 넣지 않고 컴포넌트 state(`File[]`)로 다룬다 — `content-board-sheet.tsx` 가
 * `attachedFile` 을 폼 밖에서 다루는 것과 같은 이유로, 원본 `File` 을 zod 로 검증할 실익이 없고
 * 개수·규격 검증은 `validateImageFile` 이 담당한다.
 */
export const noticeSchema = z.object({
  content: z
    .string()
    .trim()
    .min(1, NOTICE_VALIDATION_MESSAGE.CONTENT_REQUIRED)
    .max(NOTICE_CONTENT_MAX, NOTICE_VALIDATION_MESSAGE.CONTENT_TOO_LONG),
});

export type NoticeFormValues = z.infer<typeof noticeSchema>;
