/** 가게 관리 화면 헤더 정적 문구 (페이지/로딩 공용) */
export const SHOP_PAGE_COPY = {
  TITLE: "가게 관리",
  DESCRIPTION: "가게의 기본정보와 운영정보를 등록하고 관리합니다.",
  BASIC_TAB: "기본정보",
  OPERATION_TAB: "운영정보",
  EMPTY_TITLE: "보유한 가게가 없습니다",
  EMPTY_DESCRIPTION: "가게가 등록되면 이 화면에서 기본정보와 운영정보를 관리할 수 있습니다.",
} as const;

/** 전체현황·임시중지 화면 헤더 정적 문구 */
export const SHOP_STATUS_PAGE_COPY = {
  TITLE: "전체현황·임시중지",
  DESCRIPTION: "가게별 운영상태를 확인하고 영업을 임시중지합니다.",
  EMPTY_TITLE: "보유한 가게가 없습니다",
  EMPTY_DESCRIPTION: "가게가 등록되면 이 화면에서 운영상태를 확인할 수 있습니다.",
  SUSPENDED_BADGE: "임시중지",
  OPERATING_BADGE: "정상영업",
  SUSPENSION_LOAD_FAILED: "임시중지 상태를 확인할 수 없습니다",
  PERMANENTLY_CLOSED_BADGE: "폐업",
  ALL_ORDER_METHODS: "전체 주문유형",
  BULK_SUSPEND_ALL: "전체 영업임시중지",
} as const;

/** 기본정보 항목별 설정 행 라벨/안내 */
export const SHOP_BASIC_COPY = {
  THUMBNAIL_TITLE: "가게 대표이미지",
  THUMBNAIL_DESCRIPTION: "이미지는 담당자 승인 후 반영됩니다. JPG·PNG, 10MB 이하, 700×700 이상만 등록됩니다.",
  TRADEMARK_TITLE: "가게 상표",
  TRADEMARK_DESCRIPTION:
    "상표 이미지는 담당자 승인 후 반영됩니다. JPG, 900KB 이하, 560×560 이상 정사각형만 등록됩니다.",
  INTRODUCTION_TITLE: "가게 소개",
  INTRODUCTION_DESCRIPTION: "가게를 소개하는 문구를 최대 500자까지 등록할 수 있습니다.",
  CONTENT_BOARD_TITLE: "가게 콘텐츠보드",
  CONTENT_BOARD_DESCRIPTION: "이미지·GIF·동영상을 최대 4건까지 등록할 수 있습니다.",
  PHONE_NUMBER_TITLE: "가게 전화번호",
  PHONE_NUMBER_DESCRIPTION: "최대 10건까지 등록할 수 있고, 첫 등록 번호가 자동으로 대표번호가 됩니다.",
  STATUS_TITLE: "가게 상태",
  STATUS_DESCRIPTION: "노출정지를 선택하면 가게가 배민앱에서 완전히 사라집니다.",
  CONVENIENCE_TITLE: "가게 편의정보",
  CONVENIENCE_DESCRIPTION: "주차·발렛 여부, 찾아오는 길, 노출 위치, 편의시설을 등록합니다.",
  PERMANENTLY_CLOSED_BADGE: "폐업",
  IMAGE_PENDING_BADGE: "검수 대기 중",
  IMAGE_REGISTERED: "등록됨",
  IMAGE_LOAD_FAILED: "이미지를 표시할 수 없습니다",
  IMAGE_REQUEST_HISTORY: "변경 요청 이력",
  AMENITY_LEGEND: "기타 편의시설",
  AMENITY_EMPTY: "선택할 수 있는 편의시설이 없습니다.",
  PARKING_AVAILABLE: "주차 가능",
  PARKING_PAID: "주차 유료",
  VALET_AVAILABLE: "발렛 가능",
  VALET_PAID: "발렛 유료",
  NOT_REGISTERED: "미등록",
  CHANGE: "변경",
} as const;

/** 운영정보 항목별 라벨/안내 */
export const SHOP_OPERATION_COPY = {
  BUSINESS_HOURS_TITLE: "영업시간 및 휴게시간",
  BUSINESS_HOURS_DESCRIPTION: "5분 단위로 입력하며, 자정을 넘기는 영업시간도 설정할 수 있습니다.",
  CLOSED_DAYS_TITLE: "휴무일",
  CLOSED_DAYS_DESCRIPTION: "공휴일 휴무, 정기휴무, 임시휴무를 등록합니다.",
  HYGIENE_TITLE: "가게 위생 정보",
  HYGIENE_DESCRIPTION: "인증 정보는 기관 심사 결과에 따라 자동으로 반영되며 직접 수정할 수 없습니다.",
  HYGIENE_APPLY_GUIDE: "위생 인증은 식품의약품안전처 또는 세스코에 직접 신청한 뒤 심사가 완료되면 자동 반영됩니다.",
  ALL_DAY: "24시간 영업",
  CLOSED: "휴무",
  NOT_REGISTERED: "미등록",
  APPLY_TO_OTHER_DAYS: "다른 요일에도 동일하게 설정",
  BREAK_TIME_TOGGLE: "휴게시간 사용",
  HOLIDAY_CLOSED_TOGGLE: "공휴일 휴무",
  HOLIDAY_CLOSED_ON: "공휴일 휴무",
} as const;

/** 가게 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const SHOP_MESSAGE = {
  // 성공 toast
  THUMBNAIL_REQUEST_SUCCESS: "대표이미지 변경 요청이 접수되었습니다. 승인 후 반영됩니다.",
  TRADEMARK_REQUEST_SUCCESS: "가게 상표 변경 요청이 접수되었습니다. 승인 후 반영됩니다.",
  INTRODUCTION_UPDATE_SUCCESS: "가게 소개가 저장되었습니다.",
  CONTENT_BOARD_CREATE_SUCCESS: "콘텐츠보드가 등록되었습니다.",
  CONTENT_BOARD_UPDATE_SUCCESS: "콘텐츠보드가 수정되었습니다.",
  CONTENT_BOARD_DELETE_SUCCESS: "콘텐츠보드가 삭제되었습니다.",
  PHONE_NUMBER_CREATE_SUCCESS: "전화번호가 등록되었습니다.",
  PHONE_NUMBER_PRIMARY_SUCCESS: "대표번호로 지정되었습니다.",
  PHONE_NUMBER_DELETE_SUCCESS: "전화번호가 삭제되었습니다.",
  STATUS_UPDATE_SUCCESS: "가게 상태가 변경되었습니다.",
  CONVENIENCE_UPDATE_SUCCESS: "편의정보가 저장되었습니다.",
  AMENITY_CREATE_SUCCESS: "편의시설이 등록되었습니다.",
  AMENITY_DELETE_SUCCESS: "편의시설이 해제되었습니다.",
  BUSINESS_HOUR_SAVE_SUCCESS: "영업시간이 저장되었습니다.",
  HOLIDAY_CLOSED_UPDATE_SUCCESS: "공휴일 휴무 설정이 저장되었습니다.",
  CLOSED_DAY_CREATE_SUCCESS: "정기휴무가 등록되었습니다.",
  CLOSED_DAY_DELETE_SUCCESS: "정기휴무가 삭제되었습니다.",
  TEMPORARY_CLOSURE_CREATE_SUCCESS: "임시휴무가 등록되었습니다.",
  TEMPORARY_CLOSURE_DELETE_SUCCESS: "임시휴무가 삭제되었습니다.",
  SUSPENSION_CREATE_SUCCESS: "영업임시중지가 적용되었습니다.",
  SUSPENSION_RELEASE_SUCCESS: "영업임시중지가 해제되었습니다.",

  // 에러 폴백
  SUSPENSION_RESUME_UNAVAILABLE: "임시중지 해제 정보를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  UPLOAD_FAILED: "이미지 업로드에 실패했습니다. 잠시 후 다시 시도해 주세요.",
  SHOP_LIST_LOAD_FAILED: "가게 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  BASIC_INFO_LOAD_FAILED: "가게 기본정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  OPERATION_INFO_LOAD_FAILED: "가게 운영정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",

  // 검증 안내
  IMAGE_REQUIRED: "이미지를 첨부해 주세요.",
  TRADEMARK_IMAGE_TYPE: "가게 상표는 JPG 이미지만 등록할 수 있습니다.",
  TRADEMARK_IMAGE_SIZE: "가게 상표 이미지는 900KB 이하여야 합니다.",
  TRADEMARK_IMAGE_DIMENSION: "가게 상표 이미지는 560×560 이상 1:1 정사각형이어야 합니다.",
  THUMBNAIL_IMAGE_DIMENSION: "대표이미지는 700×700 이상이어야 합니다.",
  THUMBNAIL_IMAGE_SIZE: "대표이미지는 10MB 이하여야 합니다.",
  CONTENT_BOARD_IMAGE_DIMENSION: "이미지는 700×700 이상이어야 합니다.",
  CONTENT_BOARD_GIF_DIMENSION: "GIF 는 250×250 이상이어야 합니다.",
  CONTENT_BOARD_MAX_REACHED: "콘텐츠보드는 최대 4건까지 등록할 수 있습니다.",
  PHONE_NUMBER_MAX_REACHED: "전화번호는 최대 10건까지 등록할 수 있습니다.",
  REGULAR_CLOSED_DAY_MAX_REACHED: "정기휴무는 최대 15건까지 등록할 수 있습니다.",
  SUSPENSION_SHOP_REQUIRED: "임시중지할 가게를 선택해 주세요.",
} as const;
