/** 상품 화면 헤더 정적 문구 (목록/로딩 공용) */
export const PRODUCT_PAGE_COPY = {
  TITLE: "상품",
  DESCRIPTION: "매장 상품을 등록하고 옵션·이미지·카테고리를 관리합니다.",
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
