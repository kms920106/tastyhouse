/** 가게 화면 헤더 정적 문구 (목록/로딩 공용) */
export const SHOP_PAGE_COPY = {
  TITLE: "가게",
  DESCRIPTION: "가게를 등록하고 운영정보·편의시설·이미지를 관리합니다.",
} as const;

/** 이미지 검수 화면 헤더 정적 문구 (목록/로딩 공용) */
export const SHOP_IMAGE_REVIEW_PAGE_COPY = {
  TITLE: "이미지 검수",
  DESCRIPTION: "점주가 요청한 상표·대표이미지 변경을 검수합니다.",
} as const;

/** 콘텐츠보드 검수 화면 헤더 정적 문구 (목록/로딩 공용) */
export const CONTENT_BOARD_PAGE_COPY = {
  TITLE: "콘텐츠보드 검수",
  DESCRIPTION: "점주가 등록한 콘텐츠보드를 검수하고 숨김·삭제 조치합니다.",
} as const;

/** 가게 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const SHOP_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "가게가 등록되었습니다.",
  UPDATE_SUCCESS: "가게가 수정되었습니다.",
  CLOSE_SUCCESS: "가게가 폐업 처리되었습니다.",
  BUSINESS_HOUR_CREATE_SUCCESS: "운영시간이 등록되었습니다.",
  BUSINESS_HOUR_UPDATE_SUCCESS: "운영시간이 수정되었습니다.",
  BUSINESS_HOUR_DELETE_SUCCESS: "운영시간이 삭제되었습니다.",
  BREAK_TIME_CREATE_SUCCESS: "브레이크타임이 등록되었습니다.",
  BREAK_TIME_UPDATE_SUCCESS: "브레이크타임이 수정되었습니다.",
  BREAK_TIME_DELETE_SUCCESS: "브레이크타임이 삭제되었습니다.",
  CLOSED_DAY_CREATE_SUCCESS: "정기휴무일이 등록되었습니다.",
  CLOSED_DAY_DELETE_SUCCESS: "정기휴무일이 삭제되었습니다.",
  AMENITY_CATEGORY_CREATE_SUCCESS: "편의시설 카테고리가 등록되었습니다.",
  AMENITY_CATEGORY_UPDATE_SUCCESS: "편의시설 카테고리가 수정되었습니다.",
  FOOD_TYPE_CATEGORY_CREATE_SUCCESS: "음식종류 카테고리가 등록되었습니다.",
  FOOD_TYPE_CATEGORY_UPDATE_SUCCESS: "음식종류 카테고리가 수정되었습니다.",
  SHOP_AMENITY_CREATE_SUCCESS: "편의시설이 지정되었습니다.",
  SHOP_AMENITY_DELETE_SUCCESS: "편의시설이 해제되었습니다.",
  SHOP_FOOD_TYPE_CREATE_SUCCESS: "음식종류가 지정되었습니다.",
  SHOP_FOOD_TYPE_DELETE_SUCCESS: "음식종류가 해제되었습니다.",
  TAG_CREATE_SUCCESS: "태그가 등록되었습니다.",
  TAG_DELETE_SUCCESS: "태그가 삭제되었습니다.",
  ORDER_METHOD_CREATE_SUCCESS: "주문수단이 지정되었습니다.",
  ORDER_METHOD_DELETE_SUCCESS: "주문수단이 해제되었습니다.",
  BANNER_CREATE_SUCCESS: "배너 이미지가 등록되었습니다.",
  BANNER_DELETE_SUCCESS: "배너 이미지가 삭제되었습니다.",
  PHOTO_CATEGORY_CREATE_SUCCESS: "포토 카테고리가 등록되었습니다.",
  PHOTO_CATEGORY_UPDATE_SUCCESS: "포토 카테고리가 수정되었습니다.",
  PHOTO_CATEGORY_DELETE_SUCCESS: "포토 카테고리가 삭제되었습니다.",
  PHOTO_IMAGE_CREATE_SUCCESS: "이미지가 등록되었습니다.",
  PHOTO_IMAGE_UPDATE_SUCCESS: "이미지 정렬/노출이 수정되었습니다.",
  PHOTO_IMAGE_DELETE_SUCCESS: "이미지가 삭제되었습니다.",
  EDITOR_CHOICE_CREATE_SUCCESS: "테하 초이스가 등록되었습니다.",
  EDITOR_CHOICE_UPDATE_SUCCESS: "테하 초이스가 수정되었습니다.",
  EDITOR_CHOICE_DELETE_SUCCESS: "테하 초이스가 삭제되었습니다.",
  IMAGE_CHANGE_APPROVE_SUCCESS: "이미지 변경요청이 승인되었습니다.",
  IMAGE_CHANGE_REJECT_SUCCESS: "이미지 변경요청이 반려되었습니다.",
  CONTENT_BOARD_HIDE_SUCCESS: "콘텐츠보드가 숨김 처리되었습니다.",
  CONTENT_BOARD_SHOW_SUCCESS: "콘텐츠보드가 노출로 복원되었습니다.",
  CONTENT_BOARD_DELETE_SUCCESS: "콘텐츠보드가 삭제되었습니다.",
  HYGIENE_BADGE_CREATE_SUCCESS: "위생 인증 뱃지가 등록되었습니다.",
  HYGIENE_BADGE_DELETE_SUCCESS: "위생 인증 뱃지가 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  CLOSE_FAILED: "폐업 처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  IMAGE_UPLOAD_FAILED: "이미지 업로드 중 오류가 발생했습니다.",
  IMAGE_TYPE_INVALID: "jpg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.",
  IMAGE_SIZE_EXCEEDED: "이미지 크기는 최대 10MB까지 업로드할 수 있습니다.",
  IMAGE_REUPLOAD_REQUIRED: "수정 시 이미지를 다시 업로드해야 저장할 수 있습니다.",
  PHOTO_IMAGE_VISIBLE_TOGGLE_DISABLED: "API 스펙 변경으로 노출 전환이 일시적으로 지원되지 않습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "가게 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "가게 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  BUSINESS_HOURS_LOAD_FAILED: "운영시간을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  BREAK_TIMES_LOAD_FAILED: "브레이크타임을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  CLOSED_DAYS_LOAD_FAILED: "정기휴무일을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  AMENITY_CATEGORIES_LOAD_FAILED: "편의시설 카테고리를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  FOOD_TYPE_CATEGORIES_LOAD_FAILED: "음식종류 카테고리를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  SHOP_AMENITIES_LOAD_FAILED: "가게 편의시설을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  SHOP_FOOD_TYPES_LOAD_FAILED: "가게 음식종류를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  TAGS_LOAD_FAILED: "태그 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  ORDER_METHODS_LOAD_FAILED: "주문수단을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  BANNERS_LOAD_FAILED: "배너 이미지를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  PHOTO_CATEGORIES_LOAD_FAILED: "포토 카테고리를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  PHOTO_IMAGES_LOAD_FAILED: "이미지 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  EDITOR_CHOICES_LOAD_FAILED: "테하 초이스 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  STATIONS_LOAD_FAILED: "지하철역 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  IMAGE_CHANGE_REQUESTS_LOAD_FAILED: "이미지 변경요청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  HYGIENE_BADGES_LOAD_FAILED: "위생 인증 뱃지 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  CEOS_LOAD_FAILED: "점주 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  CONTENT_BOARDS_LOAD_FAILED: "콘텐츠보드 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
