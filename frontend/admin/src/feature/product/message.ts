/** 상품 화면 헤더 정적 문구 (목록/로딩 공용) */
export const PRODUCT_PAGE_COPY = {
  TITLE: "상품",
  DESCRIPTION: "매장 상품을 등록하고 옵션·이미지·카테고리를 관리합니다.",
} as const;

/** 옵션그룹 유형 배지 문구 */
export const OPTION_GROUP_TYPE_LABEL = {
  CUP_DEPOSIT: "일회용컵 보증금",
} as const;

/** 상품 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const PRODUCT_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "상품이 등록되었습니다.",
  UPDATE_SUCCESS: "상품이 수정되었습니다.",
  SOLD_OUT_SUCCESS: "상품이 품절 처리되었습니다.",
  DEACTIVATE_SUCCESS: "상품이 비활성화되었습니다.",
  OPTION_GROUP_CREATE_SUCCESS: "옵션 그룹이 등록되었습니다.",
  OPTION_CREATE_SUCCESS: "옵션이 등록되었습니다.",
  IMAGE_ADD_SUCCESS: "이미지가 등록되었습니다.",
  CATEGORY_CREATE_SUCCESS: "카테고리가 등록되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  SOLD_OUT_FAILED: "품절 처리 중 오류가 발생했습니다.",
  DEACTIVATE_FAILED: "비활성화 처리 중 오류가 발생했습니다.",
  OPTION_GROUP_CREATE_FAILED: "옵션 그룹 등록 중 오류가 발생했습니다.",
  OPTION_CREATE_FAILED: "옵션 등록 중 오류가 발생했습니다.",
  IMAGE_ADD_FAILED: "이미지 등록 중 오류가 발생했습니다.",
  CATEGORY_CREATE_FAILED: "카테고리 등록 중 오류가 발생했습니다.",
  IMAGE_UPLOAD_FAILED: "이미지 업로드 중 오류가 발생했습니다.",
  IMAGE_TYPE_INVALID: "jpg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.",
  IMAGE_SIZE_EXCEEDED: "이미지 크기는 최대 10MB까지 업로드할 수 있습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "상품 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "상품 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  OPTIONS_LOAD_FAILED: "상품 옵션을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  IMAGES_LOAD_FAILED: "상품 이미지를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  CATEGORIES_LOAD_FAILED: "카테고리 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;

// ===== 메뉴 검수 (이미지 변경 요청 · 채식 설정 요청) =====

/** 메뉴 검수 화면 정적 문구 (목록/로딩/다이얼로그 공용) */
export const PRODUCT_APPROVAL_COPY = {
  PAGE_TITLE: "메뉴 검수",
  PAGE_DESCRIPTION: "점주가 낸 메뉴 이미지·채식 설정 요청을 검수하고 승인·반려합니다.",
  TAB_IMAGE: "메뉴 이미지",
  TAB_VEGETARIAN: "채식 설정",
  APPROVE: "승인",
  REJECT: "반려",
  APPROVE_CONFIRM_TITLE: "이 요청을 승인할까요?",
  APPROVE_IMAGE_CONFIRM_BODY: "승인하면 이미지가 해당 메뉴의 이미지 목록 맨 뒤에 추가되고 손님 화면에 노출됩니다.",
  APPROVE_VEGETARIAN_CONFIRM_BODY: "승인하면 요청한 채식 단계가 메뉴에 반영됩니다.",
  REJECT_DIALOG_TITLE: "반려 사유를 입력해 주세요",
  REJECT_DIALOG_DESCRIPTION: "반려 사유는 점주에게 그대로 노출됩니다.",
  REJECT_REASON_LABEL: "반려 사유",
  REJECT_REASON_PLACEHOLDER: "반려 사유를 입력하세요",
  EMPTY: "검수할 요청이 없습니다.",
  PENDING_ONLY_FILTER: "대기 건만 보기",
  ALL_FILTER: "전체 보기",
  IMAGE_MISSING: "이미지 없음",
  PROCESSING: "처리 중...",
  CANCEL: "취소",
} as const;

/** 메뉴 검수 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const PRODUCT_APPROVAL_MESSAGE = {
  REJECT_REASON_REQUIRED: "반려 사유를 입력해 주세요.",
  REJECT_REASON_TOO_LONG: "반려 사유는 500자 이내로 입력해 주세요.",
  APPROVE_SUCCESS: "승인했습니다.",
  REJECT_SUCCESS: "반려했습니다.",
  APPROVE_FAILED: "승인에 실패했습니다.",
  REJECT_FAILED: "반려에 실패했습니다.",
  LOAD_FAILED: "메뉴 검수 목록을 불러오지 못했습니다.",
  ALREADY_PROCESSED: "이미 처리된 요청입니다. 목록을 새로고침해 주세요.",
} as const;

/** 채식 단계 배지 문구 — backend VegetarianType.description 과 일치 */
export const VEGETARIAN_TYPE_LABEL = {
  VEGAN: "비건",
  LACTO: "락토",
  OVO: "오보",
  LACTO_OVO: "락토오보",
  PESCO: "페스코",
} as const;

/** 승인 상태 배지 문구 — backend ApprovalStatus.description 과 일치 */
export const APPROVAL_STATUS_LABEL = {
  PENDING: "대기",
  APPROVED: "승인",
  REJECTED: "반려",
  CANCELED: "취소",
} as const;

/** 상태 필터 옵션 — backend ApprovalStatus enum 기준 */
export const APPROVAL_STATUS_OPTIONS = ["PENDING", "APPROVED", "REJECTED", "CANCELED"] as const;
