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

/** 라이더 안내 검수 화면 문구 */
export const SHOP_RIDER_GUIDE_ADMIN_COPY = {
  PAGE_TITLE: "라이더 안내 검수",
  PAGE_DESCRIPTION:
    "점주가 등록한 라이더 가게방문 안내를 검수하고, 기준에 맞지 않는 문구를 수정 요청하거나 삭제합니다.",
  SEARCH_PLACEHOLDER: "가게명으로 검색",
  FILTER_HAS_VISIT_GUIDE: "문구 등록된 가게만 보기",
  EMPTY_TITLE: "검수할 라이더 안내가 없습니다",
  DETAIL_VISIT_GUIDE_LEGEND: "안내 문구",
  DETAIL_PICKUP_LEGEND: "픽업 위치",
  DETAIL_HISTORY_LEGEND: "변경 이력",
  HISTORY_EMPTY: "변경 이력이 없습니다",
  VISIT_GUIDE_EMPTY: "등록된 안내 문구가 없습니다",
  PICKUP_FALLBACK_LABEL: "가게 실주소 사용",
  PICKUP_SET_LABEL: "설정됨",
  REVISION_REQUEST_ACTION: "수정 요청",
  REVISION_REQUEST_REASON_LABEL: "수정 요청 사유",
  REVISION_REQUEST_REASON_PLACEHOLDER: "예) 배차를 특정하는 문구입니다. 위치 안내로 수정해 주세요.",
  REVISION_REQUEST_CONFIRM_TITLE: "수정 요청을 등록할까요?",
  REVISION_REQUEST_CONFIRM_DESCRIPTION: "안내 문구는 그대로 유지되며, 수정 요청 이력만 남습니다.",
  DELETE_ACTION: "문구 삭제",
  DELETE_CONFIRM_TITLE: "안내 문구를 삭제할까요?",
  DELETE_CONFIRM_DESCRIPTION: "삭제하면 라이더에게 이 문구가 더 이상 표시되지 않습니다. 픽업 위치는 유지됩니다.",
  DELETE_REASON_LABEL: "삭제 사유",
  DELETE_REASON_PLACEHOLDER: "예) 가게 방문과 관련 없는 문구입니다.",
  PICKUP_EDIT_ACTION: "픽업 위치 교정",
  PICKUP_EDIT_GUIDE: "라이더 제보로 실제 위치와 다른 것이 확인된 경우 픽업 위치를 교정합니다.",
  PICKUP_ROAD_ADDRESS_LABEL: "도로명주소",
  PICKUP_LOT_ADDRESS_LABEL: "지번주소",
  PICKUP_DETAIL_ADDRESS_LABEL: "상세주소",
  PICKUP_LATITUDE_LABEL: "위도",
  PICKUP_LONGITUDE_LABEL: "경도",
} as const;

export const SHOP_RIDER_GUIDE_MESSAGE = {
  REVISION_REQUEST_SUCCESS: "수정 요청이 등록되었습니다.",
  DELETE_SUCCESS: "안내 문구가 삭제되었습니다.",
  PICKUP_UPDATE_SUCCESS: "픽업 위치가 교정되었습니다.",
  LIST_LOAD_FAILED: "라이더 안내 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "라이더 안내 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
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

/** 배달지역 조정 검수 화면 헤더 정적 문구 (목록/로딩 공용) */
export const DELIVERY_AREA_ADJUSTMENT_PAGE_COPY = {
  TITLE: "배달지역 조정",
  DESCRIPTION: "점주가 신청한 가맹점 간 배달지역 조정을 접수하고 가맹본부 전달 여부를 관리합니다.",
} as const;

export const DELIVERY_AREA_ADJUSTMENT_MESSAGE = {
  // 성공 toast
  STATUS_UPDATE_SUCCESS: "조정 신청 상태가 변경되었습니다.",
  REJECT_SUCCESS: "조정 신청이 반려되었습니다.",

  // 에러 폴백
  LIST_LOAD_FAILED: "조정 신청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "조정 신청 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
