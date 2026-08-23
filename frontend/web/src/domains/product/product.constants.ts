import type { ProductFeedbackType } from './product.dto'

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

/**
 * 메뉴 정보에 대한 고객 의견 문구.
 *
 * 리뷰(맛 평가)와 다르다 — 등록된 **정보가 틀렸다**는 제보다.
 */
export const PRODUCT_FEEDBACK_MESSAGE = {
  TYPE_REQUIRED: '의견 유형을 선택해 주세요.',
  CONTENT_REQUIRED: '기타 의견은 내용을 입력해 주세요.',
  CONTENT_TOO_LONG: '의견은 500자 이내로 입력해 주세요.',
  SUBMIT_SUCCESS: '의견을 보냈습니다. 소중한 의견 감사합니다.',
  ALREADY_SUBMITTED: '최근에 같은 의견을 보내셨습니다.',
  LOGIN_REQUIRED: '로그인 후 의견을 보낼 수 있습니다.',
  PRIVACY_NOTICE: '보내주신 의견은 사장님께 전달되며, 작성자 정보는 공개되지 않습니다.',
} as const

export const PRODUCT_FEEDBACK_COPY = {
  TRIGGER: '메뉴 정보에 대한 의견 보내기',
  TITLE: '메뉴 정보에 대한 의견을 보내주세요',
  CONTENT_LABEL: '어떤 점이 다른가요?',
  CONTENT_PLACEHOLDER: '어떤 정보가 다른지 알려주세요.',
  SUBMIT: '보내기',
  CANCEL: '취소',
} as const

/** 의견 유형 라디오. PDF 의 문구·순서를 그대로 따른다 */
export const PRODUCT_FEEDBACK_TYPE_OPTIONS = [
  { value: 'PRICE', label: '가격이 달라요' },
  { value: 'IMAGE', label: '이미지가 달라요' },
  { value: 'COMPOSITION', label: '구성이 달라요' },
  { value: 'SOLD_OUT', label: '품절인데 판매 중이에요' },
  { value: 'ETC', label: '기타' },
] as const satisfies readonly { value: ProductFeedbackType; label: string }[]

/** `ETC` 서술 내용의 최대 길이. 서버 제약과 같은 값이다 */
export const PRODUCT_FEEDBACK_CONTENT_MAX_LENGTH = 500
