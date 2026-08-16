/** 등록 가능한 문구 최대 개수 — 서버가 409(`CEO_REPLY_PHRASE_LIMIT_EXCEEDED`)로 막는 값과 같다 */
export const PHRASE_MAX_COUNT = 5;

/** 서버 `@Size(max = 50)` 과 같은 값 */
export const PHRASE_NAME_MAX_LENGTH = 50;

/** 서버 `@Size(max = 1000)` 과 같은 값 */
export const PHRASE_CONTENT_MAX_LENGTH = 1000;

/**
 * 이름 미입력 시 내용 앞부분을 표시명으로 쓰는 길이.
 *
 * **프론트는 이 값을 적용하지 않는다** — `displayName` 은 서버가 파생해 내려준다(`backend.md` 3-4-1).
 * 서버 규칙을 문서화해 두기 위한 상수이며, 이걸로 표시명을 다시 계산하면 서버와 어긋난다.
 */
export const PHRASE_DISPLAY_NAME_LENGTH = 20;
