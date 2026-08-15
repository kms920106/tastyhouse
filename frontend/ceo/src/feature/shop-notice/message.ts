/**
 * 점주 공지(사장님 공지) 한국어 문구.
 *
 * 컴포넌트·액션에 문자열을 인라인하지 않는다(`frontend/ceo/CLAUDE.md`).
 */

import { NOTICE_CONTENT_MAX, NOTICE_IMAGE_MAX_COUNT, NOTICE_IMAGE_RECOMMENDED } from "./constants";

export const SHOP_NOTICE_COPY = {
  /** 기본정보 탭 설정행 */
  ENTRY_TITLE: "사장님 공지",
  ENTRY_DESCRIPTION: "고객 화면에 노출할 공지를 등록합니다. 앱에는 1건만 노출됩니다.",

  SHEET_TITLE: "사장님 공지",
  SHEET_DESCRIPTION: "앱에는 1건만 노출됩니다. 등록해둔 다른 공지는 목록에 남습니다.",

  CONTENT_LABEL: "공지 내용",
  CONTENT_PLACEHOLDER: "고객에게 알릴 내용을 입력하세요",
  IMAGE_LABEL: `이미지 첨부 (최대 ${NOTICE_IMAGE_MAX_COUNT}장)`,
  IMAGE_GUIDE: `${NOTICE_IMAGE_RECOMMENDED} · JPG/PNG · 10MB 이하`,
  IMAGE_SELECT: "파일 선택",
  IMAGE_VALIDATING: "확인 중...",
  IMAGE_REMOVE: "제거",
  /** 수정 모드에서 새 파일을 붙이지 않았을 때의 안내 */
  IMAGE_KEEP_EXISTING: "새로 첨부하지 않으면 기존 이미지가 유지됩니다.",
  IMAGE_CLEAR_EXISTING: "기존 이미지 모두 삭제",
  IMAGE_WILL_BE_CLEARED: "저장하면 기존 이미지가 모두 삭제됩니다.",
  EXPOSE_ON_CREATE_LABEL: "등록과 동시에 앱에 반영",

  BADGE_EXPOSED: "노출중",
  BADGE_HIDDEN: "게시중단",
  IMAGE_COUNT_SUFFIX: "장",

  ACTION_PREVIEW: "미리보기",
  ACTION_EDIT: "수정",
  ACTION_DELETE: "삭제",
  ACTION_SUBMIT_CREATE: "적용",
  ACTION_SUBMIT_UPDATE: "수정",
  ACTION_CANCEL_EDIT: "수정 취소",
  ACTION_CLOSE: "닫기",
  ACTION_PENDING: "처리 중...",

  PREVIEW_TITLE: "고객 화면 미리보기",
  PREVIEW_DESCRIPTION: "저장 전 내용을 고객이 보는 형태로 확인합니다.",
  PREVIEW_BADGE: "사장님 공지",
  PREVIEW_EMPTY: "입력한 내용이 없습니다.",
  PREVIEW_CLOSE: "확인",

  LOAD_FAILED: "공지 목록을 불러오지 못했습니다.",
  CREATE_FAILED: "공지를 등록하지 못했습니다.",
  UPDATE_FAILED: "공지를 수정하지 못했습니다.",
  DELETE_FAILED: "공지를 삭제하지 못했습니다.",
  EXPOSURE_FAILED: "노출 상태를 변경하지 못했습니다.",
} as const;

export const SHOP_NOTICE_MESSAGE = {
  IMAGE_MAX_REACHED: `이미지는 최대 ${NOTICE_IMAGE_MAX_COUNT}장까지 첨부할 수 있습니다.`,
  IMAGE_SPEC_INVALID: "이미지 형식·용량·해상도를 확인해주세요. (JPG/PNG, 10MB 이하, 최소 640×280)",
  PROHIBITED_WORD: "등록할 수 없는 문구가 포함되어 있습니다.",
  /** 사전검사가 잡아낸 금칙어를 덧붙여 안내한다 */
  PROHIBITED_WORD_DETECTED: (words: string[]) => `등록할 수 없는 문구가 포함되어 있습니다: ${words.join(", ")}`,
  CREATE_SUCCESS: "공지를 등록했습니다.",
  UPDATE_SUCCESS: "공지를 수정했습니다.",
  DELETE_SUCCESS: "공지를 삭제했습니다.",
  DELETE_CONFIRM_TITLE: "공지를 삭제할까요?",
  DELETE_CONFIRM_DESCRIPTION: "삭제한 공지는 되돌릴 수 없습니다.",
  DELETE_CONFIRM_CANCEL: "취소",
  DELETE_CONFIRM_ACTION: "삭제",
  EXPOSE_SUCCESS: "앱에 반영했습니다. 기존 노출 공지는 자동으로 내려갑니다.",
  UNEXPOSE_SUCCESS: "앱에서 내렸습니다.",
  EMPTY: "등록된 공지가 없습니다.",
  NOT_FOUND: "존재하지 않는 공지입니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
} as const;

/** 폼 검증 문구 — 스키마와 액션이 같은 문장을 쓰도록 한곳에 둔다 */
export const NOTICE_VALIDATION_MESSAGE = {
  CONTENT_REQUIRED: "공지 내용을 입력해주세요.",
  CONTENT_TOO_LONG: `공지 내용은 ${NOTICE_CONTENT_MAX.toLocaleString("ko-KR")}자 이내로 입력해주세요.`,
} as const;

/**
 * 서버 `errorCode` → 사용자 문구.
 *
 * 서버 `message` 를 그대로 노출하지 않는 이유는 `shop-review/message.ts` 와 같다 —
 * 점주 화면 문장이 백엔드 문구 변경에 흔들리지 않게 한다. 표에 없는 코드는 호출부 기본 문구로 떨어진다.
 */
export const SHOP_NOTICE_ERROR_MESSAGE: Record<string, string> = {
  SHOP_TEXT_PROHIBITED_WORD: SHOP_NOTICE_MESSAGE.PROHIBITED_WORD,
  SHOP_IMAGE_SPEC_INVALID: SHOP_NOTICE_MESSAGE.IMAGE_SPEC_INVALID,
  SHOP_NOTICE_IMAGE_LIMIT_EXCEEDED: SHOP_NOTICE_MESSAGE.IMAGE_MAX_REACHED,
  SHOP_NOTICE_NOT_FOUND: SHOP_NOTICE_MESSAGE.NOT_FOUND,
  SHOP_ACCESS_DENIED: "접근 권한이 없는 가게입니다.",
};
