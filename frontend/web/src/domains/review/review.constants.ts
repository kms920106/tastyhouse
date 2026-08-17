import type { ReviewBlindReason } from './review.types'

/** 게시중단 사유 코드 ↔ 한국어 라벨. 서버가 `reasonDescription`을 안 내리면 이 표로 대체한다. */
export const REVIEW_BLIND_REASON_LABEL: Record<ReviewBlindReason, string> = {
  ADVERTISEMENT: '광고성 리뷰',
  PROFANITY: '욕설·비방',
  IRRELEVANT: '주문과 무관한 내용',
  PRIVACY: '개인정보 노출',
  ETC: '기타',
}

/** 리뷰 게시중단 삭제 동의 화면 문구 — 컴포넌트에 인라인하지 않는다. */
export const REVIEW_BLIND_CONSENT_COPY = {
  PAGE_TITLE: '리뷰 게시중단 안내',
  HEADER_TITLE: '리뷰 게시중단 안내',

  /** 게시중단 사실 안내 */
  NOTICE_TITLE: '작성하신 리뷰가 게시중단되었습니다.',
  NOTICE_DESCRIPTION:
    '사장님의 게시중단 요청이 검토를 거쳐 승인되어 현재 이 리뷰는 다른 이용자에게 보이지 않습니다.',

  REASON_LABEL: '게시중단 사유',
  DETAIL_REASON_LABEL: '상세 사유',
  BLIND_UNTIL_LABEL: '재노출 예정일',
  BLIND_UNTIL_EMPTY: '-',
  REVIEW_CONTENT_LABEL: '작성하신 리뷰',

  /** 선택 안내 */
  CHOICE_TITLE: '리뷰를 삭제하시겠습니까?',
  CHOICE_DESCRIPTION:
    '삭제에 동의하시면 이 리뷰는 즉시 삭제되며 되돌릴 수 없습니다.\n동의하지 않으시면 재노출 예정일에 자동으로 다시 표시됩니다.',

  CONSENT_BUTTON: '삭제에 동의합니다',
  REJECT_BUTTON: '동의하지 않습니다',

  /** 동의 확인 다이얼로그 */
  CONFIRM_TITLE: '리뷰를 삭제할까요?',
  CONFIRM_DESCRIPTION: '삭제된 리뷰는 복구할 수 없습니다.\n정말 삭제에 동의하시겠습니까?',
  CONFIRM_LABEL: '삭제 동의',
  CANCEL_LABEL: '취소',

  /** 결과 토스트 */
  CONSENT_SUCCESS: '리뷰가 삭제되었습니다.',
  REJECT_SUCCESS: '30일 후 자동으로 다시 표시됩니다.',
  SUBMIT_FAILED: '처리에 실패했습니다. 잠시 후 다시 시도해 주세요.',
  /** 409 `REVIEW_BLIND_REQUEST_NOT_APPROVED` — 이미 재노출·삭제되어 처리 대상이 아님 */
  NOT_APPROVED: '이미 처리된 리뷰입니다.',

  /** 조회 실패 */
  FETCH_FAILED: '게시중단 정보를 불러오지 못했습니다.',
} as const

/** 게시중단 동의/거부 API가 내려주는 비즈니스 에러 코드. */
export const REVIEW_BLIND_ERROR_CODE = {
  /** 리뷰가 없거나 타인의 리뷰 — 존재를 숨기기 위해 404로 통일된다. */
  REVIEW_NOT_FOUND: 'REVIEW_NOT_FOUND',
  /** 게시중단(APPROVED) 상태가 아님 — 이미 재노출됐거나 삭제됨. */
  REVIEW_BLIND_REQUEST_NOT_APPROVED: 'REVIEW_BLIND_REQUEST_NOT_APPROVED',
} as const
