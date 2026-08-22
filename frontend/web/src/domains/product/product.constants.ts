/**
 * 상품 도메인의 한국어 문구·라벨.
 *
 * 코드 → 한국어 변환을 컴포넌트가 직접 하지 않는다(`src/domains/CLAUDE.md`). 화면은 이 표를 읽는다.
 */

/** 가격 체계(가격명·채널별 가격) 관련 문구 */
export const PRODUCT_PRICE_COPY = {
  /** 가격이 2개 이상인 메뉴는 주문 전 하나를 골라야 한다 */
  SELECT_SECTION_TITLE: '가격 선택',
  SELECT_REQUIRED: '가격을 선택해 주세요.',

  /**
   * 주문 금액 불일치.
   *
   * 서버가 클라이언트 금액과 서버 계산 금액을 대조해 거절한 상황(`ORDER_PRODUCT_AMOUNT_MISMATCH`)이다.
   * 대개 점주가 그 사이 가격을 바꾼 것이므로, 재시도가 아니라 **재조회**를 안내한다.
   */
  AMOUNT_MISMATCH: '가격 정보가 변경되었습니다. 새로고침 후 다시 시도해 주세요.',
} as const

/**
 * 가게 가격 뱃지 문구.
 *
 * 노출 조건은 서버가 판정해 boolean 으로 내려준다 — 화면이 가격을 비교해 판정하지 않는다.
 */
export const SHOP_PRICE_BADGE_COPY = {
  SAME_AS_STORE_PRICE: '매장과 같은 가격',
  STORE_PRICE_PICKUP: '매장가격 픽업',
} as const
