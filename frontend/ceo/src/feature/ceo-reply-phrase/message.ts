import { PHRASE_CONTENT_MAX_LENGTH, PHRASE_MAX_COUNT, PHRASE_NAME_MAX_LENGTH } from "./constants";

export const CEO_REPLY_PHRASE_COPY = {
  ENTRY_TITLE: "자주 쓰는 문구 관리",
  TITLE: "자주 쓰는 문구",
  DESCRIPTION: `답변에 자주 쓰는 문구를 ${PHRASE_MAX_COUNT}개까지 저장해 두고 불러올 수 있습니다.`,

  // 목록
  EMPTY: "등록된 문구가 없습니다.",
  LOAD_FAILED: "자주 쓰는 문구를 불러오지 못했습니다.",
  LIMIT_REACHED: `문구는 최대 ${PHRASE_MAX_COUNT}개까지 등록할 수 있습니다.`,

  // 폼
  ADD: "문구 추가",
  NAME_LABEL: "문구 이름 (선택)",
  NAME_PLACEHOLDER: "미입력 시 내용 앞부분이 이름으로 표시됩니다",
  CONTENT_LABEL: "문구 내용",
  CONTENT_PLACEHOLDER: "자주 쓰는 답변 문구를 입력하세요.",
  SUBMIT_CREATE: "등록",
  SUBMIT_UPDATE: "수정 완료",
  CANCEL: "취소",
  EDIT: "수정",
  DELETE: "삭제",

  // 삭제 확인
  DELETE_CONFIRM_TITLE: "문구를 삭제할까요?",
  DELETE_CONFIRM_DESCRIPTION: "삭제한 문구는 되돌릴 수 없습니다. 필요하면 다시 등록할 수 있습니다.",
  DELETE_CONFIRM_ACTION: "삭제하기",
  DELETE_CONFIRM_DISMISS: "닫기",

  // 결과
  CREATE_SUCCESS: "문구를 등록했습니다.",
  UPDATE_SUCCESS: "문구를 수정했습니다.",
  DELETE_SUCCESS: "문구를 삭제했습니다.",
  CREATE_FAILED: "문구를 등록하지 못했습니다.",
  UPDATE_FAILED: "문구를 수정하지 못했습니다.",
  DELETE_FAILED: "문구를 삭제하지 못했습니다.",

  // 답변 폼의 문구 선택 영역
  PICKER_TITLE: "자주 쓰는 문구",
  PICKER_PLACEHOLDER: "문구를 선택하세요",
  PICKER_APPLY: "사용하기",
} as const;

export const CEO_REPLY_PHRASE_MESSAGE = {
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
} as const;

export const CEO_REPLY_PHRASE_ERROR_MESSAGE: Record<string, string> = {
  CEO_REPLY_PHRASE_NOT_FOUND: "문구를 찾을 수 없습니다. 목록을 새로 불러와주세요.",
  CEO_REPLY_PHRASE_LIMIT_EXCEEDED: `문구는 최대 ${PHRASE_MAX_COUNT}개까지 등록할 수 있습니다.`,
  CEO_REPLY_PHRASE_ACCESS_DENIED: "다른 점주의 문구입니다.",
  SHOP_TEXT_PROHIBITED_WORD: "문구에 사용할 수 없는 단어가 포함되어 있습니다.",
};

export const CEO_REPLY_PHRASE_VALIDATION_MESSAGE = {
  CONTENT_REQUIRED: "문구 내용을 입력해주세요.",
  CONTENT_MAX_LENGTH: `문구 내용은 ${PHRASE_CONTENT_MAX_LENGTH.toLocaleString("ko-KR")}자 이내로 입력해주세요.`,
  NAME_MAX_LENGTH: `문구 이름은 ${PHRASE_NAME_MAX_LENGTH}자 이내로 입력해주세요.`,
} as const;
