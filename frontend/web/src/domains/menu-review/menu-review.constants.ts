/** 메뉴 평가 코멘트 최대 길이 — 서버 `@Size(max = 300)`과 같은 값 */
export const MENU_REVIEW_COMMENT_MAX_LENGTH = 300

/** 메뉴 평가 별점 범위 — 서버 `@Min(1) @Max(5)`와 같은 값 */
export const MENU_REVIEW_RATING_MIN = 1
export const MENU_REVIEW_RATING_MAX = 5

/** 상품 상세 메뉴 평가 목록 한 페이지 크기 */
export const MENU_REVIEW_PAGE_SIZE = 5

/** 메뉴 평가 화면 문구 — 컴포넌트에 인라인하지 않는다. */
export const MENU_REVIEW_COPY = {
  SECTION_TITLE: '메뉴 평가',
  SECTION_DESCRIPTION: '주문한 메뉴는 매장 리뷰와 별개로 평가할 수 있어요.',
  COMMENT_PLACEHOLDER: '메뉴는 어떠셨나요? (선택)',
  COMMENT_TOGGLE: '코멘트 남기기',
  SAVE: '평가 저장',
  UPDATE: '평가 수정',
  SAVED: '메뉴 평가가 저장되었습니다.',
  RATING_REQUIRED: '별점을 선택해 주세요.',
  LIST_TITLE: '메뉴 평가',
  LIST_COUNT: (count: number) => `${count}개의 메뉴 평가`,
  LIST_EMPTY: '아직 등록된 메뉴 평가가 없습니다.',
}

/**
 * 서버 `ErrorCode` → 사용자 노출 문구.
 *
 * `MENU_REVIEW_ALREADY_EXISTS`를 받으면 목록을 재조회해 수정 모드로 전환한다.
 */
export const MENU_REVIEW_ERROR_MESSAGES: Record<string, string> = {
  MENU_REVIEW_ALREADY_EXISTS: '이미 평가한 메뉴예요.',
  MENU_REVIEW_NOT_ALLOWED: '평가할 수 없는 메뉴예요.',
  MENU_REVIEW_ACCESS_DENIED: '본인이 주문한 메뉴만 평가할 수 있어요.',
  MENU_REVIEW_NOT_FOUND: '메뉴 평가를 찾을 수 없어요.',
}

export const getMenuReviewErrorMessage = (errorCode?: string): string =>
  (errorCode && MENU_REVIEW_ERROR_MESSAGES[errorCode]) ||
  '오류가 발생했습니다. 다시 시도해 주세요.'
