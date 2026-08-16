/**
 * 점주 리뷰 관리 한국어 문구.
 *
 * 컴포넌트·액션에 문자열을 인라인하지 않는다(`frontend/ceo/CLAUDE.md`).
 */
export const SHOP_REVIEW_COPY = {
  ENTRY_TITLE: "리뷰 관리",
  TITLE: "리뷰 관리",
  DESCRIPTION: "가게에 등록된 리뷰를 확인하고 답변합니다.",

  // ===== 앱 노출 정렬 설정 =====
  SORT_TYPE_SECTION_TITLE: "앱 노출 정렬 설정",
  SORT_TYPE_GUIDE: "고객이 앱에서 보는 리뷰의 기본 정렬 순서입니다. 고객은 직접 정렬을 바꿔 볼 수도 있습니다.",
  SORT_TYPE_LABEL: "기본 정렬",
  SORT_TYPE_SAVE: "저장",
  SORT_TYPE_SAVED: "앱 노출 정렬을 저장했습니다.",
  SORT_TYPE_SAVE_FAILED: "정렬 설정을 저장하지 못했습니다.",
  SORT_TYPE_LOAD_FAILED: "정렬 설정을 불러오지 못했습니다.",
  SORT_TYPE_NOT_CONFIGURED: "아직 설정하지 않아 최신순으로 노출됩니다.",
  SORT_TYPE_UPDATED_AT: "마지막 저장",

  // ===== 통계 =====
  STATISTICS_SECTION_TITLE: "리뷰 통계",
  STATISTICS_LOAD_FAILED: "리뷰 통계를 불러오지 못했습니다.",
  STAT_AVERAGE_RATING: "평균 별점",
  STAT_TOTAL_REVIEW_COUNT: "리뷰 수",
  STAT_TOTAL_REVIEW_COUNT_HINT: "최근 6개월",
  STAT_RECENT_REVIEW_COUNT: "최근 리뷰 수",
  STAT_RECENT_REVIEW_COUNT_HINT: "최근 30일",
  STAT_WILL_REVISIT: "재방문 의사율",
  STAT_WILL_REVISIT_HINT: "재방문하겠다고 답한 비율",
  RATING_DISTRIBUTION_TITLE: "별점 분포",
  ASPECT_RATING_TITLE: "항목별 평균",
  MONTHLY_CHART_TITLE: "월별 추이",
  MONTHLY_CHART_DESCRIPTION: "최근 6개월 평균 별점과 리뷰 수입니다.",
  CHART_AVERAGE_RATING: "평균 별점",
  CHART_REVIEW_COUNT: "리뷰 수",

  // ===== 필터 =====
  FILTER_PERIOD_LABEL: "작성 기간",
  FILTER_RATING_LABEL: "별점",
  FILTER_ORDER_METHOD_LABEL: "주문유형",
  FILTER_SORT_TYPE_LABEL: "정렬",
  FILTER_HAS_IMAGE_LABEL: "사진 있는 리뷰만",
  FILTER_ALL: "전체",
  FILTER_RESET: "초기화",
  /** 별점 필터 옵션 라벨을 만드는 접미어 (예: "5점") */
  RATING_SUFFIX: "점",

  // ===== 목록 =====
  EMPTY_TITLE: "리뷰가 없습니다",
  EMPTY_DESCRIPTION: "선택한 조건에 해당하는 리뷰가 없습니다. 필터를 바꿔 다시 조회해보세요.",
  NO_SHOP_TITLE: "관리할 가게가 없습니다",
  NO_SHOP_DESCRIPTION: "가게가 등록되면 리뷰를 확인할 수 있습니다.",
  BADGE_ANSWERED: "답변완료",
  BADGE_UNANSWERED: "미답변",
  BADGE_BLINDED: "차단됨",
  BADGE_UNVERIFIED: "미인증 리뷰",
  /** 작성자가 비공개로 등록한 리뷰. 차단 뱃지와 동시에 뜰 수 있어 별개 뱃지로 둔다 */
  BADGE_OWNER_ONLY: "사장님만보기",
  PREVIOUS_PAGE: "이전",
  NEXT_PAGE: "다음",

  // ===== 상세 =====
  DETAIL_ACTION: "상세 보기",
  DETAIL_TITLE: "리뷰 정보",
  DETAIL_LOAD_FAILED: "리뷰 상세를 불러오지 못했습니다.",
  REVIEW_NUMBER: "리뷰번호",
  REVIEW_CONTENT: "리뷰 내용",
  REVIEW_IMAGES: "리뷰 사진",
  ORDER_PRODUCTS: "주문 메뉴",
  ORDER_METHOD: "주문유형",
  WRITTEN_AT: "작성일",
  WILL_REVISIT: "재방문 의사",
  WILL_REVISIT_YES: "있음",
  WILL_REVISIT_NO: "없음",
  REVIEW_TAGS: "리뷰 태그",
  VALUE_ABSENT: "-",

  // ===== 사장님 답변 =====
  // 배달 평가 — 고객 앱에는 노출되지 않는 점주 전용 정보다
  DELIVERY_RATING_SECTION_TITLE: "배달 평가",
  DELIVERY_RATING_LABEL: "배달 평점",
  DELIVERY_COMMENT_LABEL: "배달 평가 내용",
  DELIVERY_RATING_OWNER_ONLY_GUIDE: "배달 평가는 고객 앱에 노출되지 않고 사장님만 볼 수 있습니다.",
  OWNER_REPLY_SECTION_TITLE: "사장님 댓글",
  OWNER_REPLY_PLACEHOLDER: "고객에게 전할 답변을 입력하세요.",
  OWNER_REPLY_SUBMIT: "답변 등록",
  OWNER_REPLY_EDIT: "수정",
  OWNER_REPLY_EDIT_SUBMIT: "수정 완료",
  OWNER_REPLY_EDIT_CANCEL: "취소",
  OWNER_REPLY_DELETE: "삭제",
  OWNER_REPLY_EMPTY: "아직 답변하지 않은 리뷰입니다.",
  /** 날짜는 호출부에서 `formatDate` 로 포맷해 주입한다 */
  OWNER_REPLY_DEADLINE_GUIDE: (deadline: string) => `${deadline}까지 답변할 수 있어요`,
  OWNER_REPLY_PERIOD_EXPIRED: "리뷰 작성일로부터 30일이 지나 답변을 등록할 수 없습니다.",
  /** 비공개 리뷰의 답글도 고객 화면에 노출되지 않는다는 안내 */
  OWNER_REPLY_OWNER_ONLY_GUIDE: "이 리뷰의 답글도 고객에게 공개되지 않습니다.",
  OWNER_REPLY_CREATED_AT: "답변일",
  OWNER_REPLY_UPDATED_AT: "수정일",
  OWNER_REPLY_CREATE_SUCCESS: "답변을 등록했습니다.",
  OWNER_REPLY_UPDATE_SUCCESS: "답변을 수정했습니다.",
  OWNER_REPLY_DELETE_SUCCESS: "답변을 삭제했습니다.",
  OWNER_REPLY_CREATE_FAILED: "답변을 등록하지 못했습니다.",
  OWNER_REPLY_UPDATE_FAILED: "답변을 수정하지 못했습니다.",
  OWNER_REPLY_DELETE_FAILED: "답변을 삭제하지 못했습니다.",
  OWNER_REPLY_DELETE_CONFIRM_TITLE: "답변을 삭제할까요?",
  OWNER_REPLY_DELETE_CONFIRM_DESCRIPTION: "삭제한 답변은 되돌릴 수 없습니다. 필요하면 다시 등록할 수 있습니다.",
  OWNER_REPLY_DELETE_CONFIRM_ACTION: "삭제하기",
  OWNER_REPLY_DELETE_CONFIRM_DISMISS: "닫기",

  // ===== 게시중단 요청 =====
  BLIND_REQUEST_ACTION: "게시중단 요청",
  BLIND_REQUEST_TITLE: "게시중단 요청",
  BLIND_REQUEST_DESCRIPTION: "관리자 심사를 거쳐 승인되면 이 리뷰가 고객 화면에서 숨겨집니다.",
  BLIND_REQUEST_REASON_LABEL: "요청 사유",
  BLIND_REQUEST_REASON_PLACEHOLDER: "사유를 선택하세요",
  BLIND_REQUEST_DETAIL_LABEL: "상세 사유",
  BLIND_REQUEST_DETAIL_PLACEHOLDER: "관리자가 판단할 수 있도록 상황을 구체적으로 적어주세요.",
  BLIND_REQUEST_SUBMIT: "요청하기",
  BLIND_REQUEST_SUCCESS: "게시중단을 요청했습니다.",
  BLIND_REQUEST_FAILED: "게시중단을 요청하지 못했습니다.",
  BLIND_REASON_LOAD_FAILED: "게시중단 요청 사유를 불러오지 못했습니다.",
  BLIND_HISTORY_SECTION_TITLE: "게시중단 요청 이력",
  BLIND_HISTORY_EMPTY: "게시중단을 요청한 이력이 없습니다.",
  BLIND_HISTORY_REQUESTED_AT: "요청일",
  BLIND_HISTORY_DETAIL_REASON: "상세 사유",
  BLIND_HISTORY_REJECT_REASON: "반려 사유",
  BLIND_CANCEL_ACTION: "요청 취소",
  BLIND_CANCEL_CONFIRM_TITLE: "게시중단 요청을 취소할까요?",
  BLIND_CANCEL_CONFIRM_DESCRIPTION: "취소하면 심사가 중단됩니다. 필요하면 같은 리뷰에 다시 요청할 수 있습니다.",
  BLIND_CANCEL_CONFIRM_ACTION: "취소하기",
  BLIND_CANCEL_CONFIRM_DISMISS: "닫기",
  BLIND_CANCEL_SUCCESS: "게시중단 요청을 취소했습니다.",
  BLIND_CANCEL_FAILED: "게시중단 요청을 취소하지 못했습니다.",

  // ===== 이미지 =====
  IMAGE_LOAD_FAILED: "이미지를 불러오지 못했습니다.",
  IMAGE_DIALOG_TITLE: "리뷰 사진",
  IMAGE_DIALOG_CLOSE: "닫기",
  /** 여러 장일 때 "2 / 5" 형태를 만들기 위한 구분자 */
  IMAGE_COUNT_SEPARATOR: " / ",

  // ===== 조회 실패 =====
  LOAD_FAILED: "리뷰 목록을 불러오지 못했습니다.",
  SHOP_ACCESS_DENIED: "내 가게의 리뷰만 관리할 수 있습니다.",
  SHOP_NOT_FOUND: "가게를 찾을 수 없습니다.",
  DATE_RANGE_INVALID: "조회 시작일이 종료일보다 늦을 수 없습니다.",
} as const;

/** `error.tsx` 로 던지는 치명적 실패 문구 */
export const SHOP_REVIEW_MESSAGE = {
  SHOP_LIST_LOAD_FAILED: "내 가게 목록을 불러오지 못했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
} as const;

/**
 * 서버 `errorCode` → 사용자 문구.
 *
 * 서버가 내려주는 `message` 를 그대로 쓰지 않고 이 표를 거치는 이유는, 점주 화면에서
 * 읽히는 문장이 백엔드 문구 변경에 흔들리지 않게 하기 위함이다. 표에 없는 코드는
 * 호출부의 기본 문구로 떨어진다.
 */
export const SHOP_REVIEW_ERROR_MESSAGE: Record<string, string> = {
  SHOP_ACCESS_DENIED: "내 가게의 리뷰만 관리할 수 있습니다.",
  REVIEW_NOT_FOUND: "리뷰를 찾을 수 없습니다. 목록을 새로 불러와주세요.",
  REVIEW_OWNER_REPLY_ALREADY_EXISTS: "이미 답변을 등록한 리뷰입니다.",
  REVIEW_OWNER_REPLY_PERIOD_EXPIRED: "리뷰 작성일로부터 30일이 지나 답변을 등록할 수 없습니다.",
  REVIEW_OWNER_REPLY_NOT_FOUND: "답변을 찾을 수 없습니다. 이미 삭제되었을 수 있습니다.",
  REVIEW_BLIND_REQUEST_ALREADY_PENDING: "이미 게시중단 요청이 접수된 리뷰입니다.",
  REVIEW_BLIND_REQUEST_NOT_PENDING: "이미 처리된 요청은 취소할 수 없습니다.",
  REVIEW_BLIND_DETAIL_REASON_REQUIRED: "기타 사유는 상세 내용을 입력해주세요.",
  SHOP_TEXT_PROHIBITED_WORD: "답변에 사용할 수 없는 단어가 포함되어 있습니다.",
  REVIEW_DATE_RANGE_INVALID: "조회 시작일이 종료일보다 늦을 수 없습니다.",
};

/** 폼 검증 문구 — 스키마와 액션이 같은 문장을 쓰도록 한곳에 둔다 */
export const SHOP_REVIEW_VALIDATION_MESSAGE = {
  OWNER_REPLY_REQUIRED: "답변 내용을 입력해주세요.",
  OWNER_REPLY_MAX_LENGTH: "답변은 1,000자 이내로 입력해주세요.",
  BLIND_REASON_REQUIRED: "요청 사유를 선택해주세요.",
  BLIND_DETAIL_REASON_REQUIRED: "기타 사유는 상세 내용을 입력해주세요.",
  BLIND_DETAIL_REASON_MAX_LENGTH: "상세 사유는 500자 이내로 입력해주세요.",
  SORT_TYPE_REQUIRED: "정렬 순서를 선택해주세요.",
} as const;
