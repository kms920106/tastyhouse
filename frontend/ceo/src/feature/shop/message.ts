/** 가게 관리 화면 헤더 정적 문구 (페이지/로딩 공용) */
export const SHOP_PAGE_COPY = {
  TITLE: "가게 관리",
  DESCRIPTION: "가게의 기본정보와 운영정보를 등록하고 관리합니다.",
  BASIC_TAB: "기본정보",
  OPERATION_TAB: "운영정보",
  ORDER_TAB: "주문정보",
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
  MIN_ORDER_AMOUNT_TITLE: "최소주문금액",
  MIN_ORDER_AMOUNT_DESCRIPTION:
    "고객이 주문할 수 있는 최소 금액입니다. 할인 후 금액을 기준으로 적용되며, 포장 주문에는 적용되지 않습니다.",
  MIN_ORDER_AMOUNT_UNSET_LABEL: "미설정 (제한 없음)",
  MIN_ORDER_AMOUNT_GUIDE: "5,000원 ~ 30,000원 내에서 입력할 수 있습니다. 0을 입력하면 제한이 없습니다.",
  SCHEDULED_ORDER_TITLE: "예약주문",
  SCHEDULED_ORDER_DESCRIPTION: "고객이 음식을 받을 시간을 미리 정해 주문할 수 있습니다. 배달·포장 주문에만 적용됩니다.",
  SCHEDULED_ORDER_ON_LABEL: "설정함",
  SCHEDULED_ORDER_OFF_LABEL: "설정안함",
  SCHEDULED_ORDER_GUIDE:
    "배달은 영업 시작 2시간 이후, 포장은 1시간 이후부터 예약을 받습니다. 당일 영업시간 내에서만 예약할 수 있습니다(24시간 운영 가게는 24시간 이후까지). 예약주문을 해제해도 이미 접수된 예약은 그대로 진행됩니다.",
  DELIVERY_TIP_TITLE: "배달팁",
  DELIVERY_TIP_DESCRIPTION:
    "주문금액 구간에 따라 배달팁을 다르게 받을 수 있습니다. 판정 기준은 상품 할인 후 금액입니다.",
  DELIVERY_TIP_UNSET_LABEL: "미설정",
  DELIVERY_TIP_GUIDE:
    "구간은 최대 3개까지 설정할 수 있고, 배달팁은 5,000원 미만만 입력할 수 있습니다. 주문금액이 높아질수록 배달팁이 낮아져야 합니다.",
  EXTRA_DELIVERY_TIP_TITLE: "추가 배달팁",
  EXTRA_DELIVERY_TIP_DESCRIPTION:
    "거리·지역·시간대·공휴일에 따라 배달팁을 추가로 받을 수 있습니다. 각 항목은 10,000원 이하로 설정합니다.",
  DISTANCE_REGION_EXCLUSIVE_GUIDE: "거리별과 지역별은 함께 사용할 수 없습니다. 한 가지만 선택해 주세요.",
  HOLIDAY_TIP_SUNDAY_GUIDE: "일요일에는 공휴일 배달팁이 적용되지 않습니다. 시간별 배달팁을 이용해 주세요.",
  DELIVERY_AREA_EMPTY_GUIDE: "먼저 배달가능지역을 등록해 주세요.",
  DELIVERY_AREA_TITLE: "배달가능지역",
  DELIVERY_AREA_DESCRIPTION:
    "배달할 수 있는 행정동을 등록합니다. 지역별 배달팁은 여기 등록된 지역 중에서만 설정할 수 있습니다.",
  DELIVERY_AREA_SEARCH_LABEL: "행정동 검색",
  DELIVERY_AREA_SEARCH_PLACEHOLDER: "시/군/구 또는 동 이름으로 검색",
  DELIVERY_AREA_SEARCH_EMPTY: "검색 결과가 없습니다.",
  DELIVERY_AREA_LIST_EMPTY: "등록된 배달가능지역이 없습니다.",
  DELIVERY_AREA_LIST_LEGEND: "등록된 지역",
  DELIVERY_AREA_MAP_TITLE: "배달지역 설정",
  DELIVERY_AREA_MAP_GUIDE: "반경이나 행정동으로 큰 틀을 잡고, 그리기·지우기로 다듬어 보세요.",
  DELIVERY_AREA_ZOOM_IN_HINT: "지도를 확대하면 배달지역을 편집할 수 있습니다.",
  DELIVERY_AREA_MISSING_CHECK_HINT: "설정을 마쳤다면 지도를 확대해 빠진 구역이 없는지 확인해 주세요.",
  DELIVERY_AREA_RADIUS_LABEL: "반경으로 추가",
  DELIVERY_AREA_RADIUS_HELP:
    "가게 주소를 기준으로 반경 안의 행정동을 한 번에 추가합니다. 최대 7km까지 설정할 수 있습니다.",
  DELIVERY_AREA_RADIUS_RULE: "행정동 대표 지점이 반경 안에 들면 추가됩니다.",
  DELIVERY_AREA_TREE_LABEL: "행정동 선택",
  DELIVERY_AREA_LOCKED_BY_TIP: "지역별 배달팁이 설정된 지역이라 해제할 수 없습니다.",
  DELIVERY_AREA_EMPTY_WARNING: "배달지역을 모두 해제하면 전 지역 배달로 간주됩니다.",
  DELIVERY_AREA_MAP_UNAVAILABLE: "지도를 불러올 수 없습니다. 검색으로 지역을 선택해 주세요.",
  DELIVERY_AREA_PANEL_OPEN: "지역 설정 열기",
  DELIVERY_AREA_PANEL_CLOSE: "지도로 돌아가기",
  DELIVERY_AREA_CENTER_MOVED: "가게 주소가 바뀌었습니다. 배달지역을 다시 설정해 주세요.",
  ADJUSTMENT_TITLE: "배달지역 조정 신청",
  ADJUSTMENT_GUIDE: "다른 가맹점과 배달지역이 중첩되어 조정이 필요하면 가맹본부에 중재를 신청할 수 있습니다.",
  ADJUSTMENT_NOTICE:
    "접수 내용은 가맹본부에 전달되며, 조정 절차의 개시 여부와 결과는 가맹본부가 결정합니다. 조정이 성립된 경우에만 배달지역에 반영됩니다.",
  ADJUSTMENT_PENDING_GUIDE: "이미 진행 중인 조정 신청이 있습니다.",
  ADJUSTMENT_HISTORY_LEGEND: "신청 이력",
  ADJUSTMENT_HISTORY_EMPTY: "신청 이력이 없습니다.",
  ADJUSTMENT_COUNTERPART_SHOP_NAME_LABEL: "상대 가맹점 상호명",
  ADJUSTMENT_COUNTERPART_BUSINESS_NUMBER_LABEL: "상대 가맹점 사업자등록번호",
  ADJUSTMENT_COUNTERPART_BUSINESS_NUMBER_GUIDE: "하이픈 없이 숫자 10자리로 입력해 주세요.",
  ADJUSTMENT_FRANCHISE_NAME_LABEL: "가맹본부명",
  ADJUSTMENT_REASON_LABEL: "배달지역 중첩 사유",
  ADJUSTMENT_CONSENT_FILE_LABEL: "조정신청 관련 정보제공 동의서",
  ADJUSTMENT_CONSENT_FILE_GUIDE: "jpg, png, gif, webp, pdf 파일을 10MB 이하로 첨부해 주세요.",
  ADJUSTMENT_CONSENT_FILE_UNSELECTED: "선택된 파일이 없습니다.",
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

/**
 * 주문정보 탭 문구.
 *
 * 주문불가 사유는 서버가 `unavailableReasonName` 으로 한글 문구를 내려주므로 이 파일에 사유 매핑을 두지 않는다.
 * 사유가 추가돼도 프론트를 고칠 필요가 없다.
 */
export const SHOP_ORDER_COPY = {
  SHOP_STATUS_TITLE: "가게 주문가능 상태",
  SHOP_STATUS_DESCRIPTION: "가게 전체의 주문 접수 가능 여부입니다. 영업시간·휴무일·임시중지에 따라 자동으로 바뀝니다.",
  ORDER_METHOD_TITLE: "주문유형별 주문가능 상태",
  ORDER_METHOD_DESCRIPTION: "가게에 배정된 주문유형별 접수 가능 여부입니다. 배정 변경은 담당자에게 문의해 주세요.",
  AVAILABLE_BADGE: "가능",
  UNAVAILABLE_BADGE: "불가",
  ORDER_METHOD_EMPTY_TITLE: "배정된 주문유형이 없습니다",
  ORDER_METHOD_EMPTY_DESCRIPTION: "주문유형이 배정되면 이 화면에서 유형별 주문가능 상태를 확인할 수 있습니다.",
  ORDER_AVAILABILITY_LOAD_FAILED: "주문가능 상태를 확인할 수 없습니다",
} as const;

/** 라이더 가게방문 안내 · 픽업 위치 문구 */
export const SHOP_RIDER_COPY = {
  VISIT_GUIDE_TITLE: "라이더 가게방문 안내",
  VISIT_GUIDE_DESCRIPTION:
    "라이더가 가게를 쉽게 찾아 음식을 받아갈 수 있도록 안내 문구를 등록합니다. 라이더 앱에만 표시되며 고객에게는 보이지 않습니다.",
  VISIT_GUIDE_PLACEHOLDER: "예) 대로변에서 분홍색 건물 1층 OO 안경 옆 가게입니다.",
  VISIT_GUIDE_RIDER_ONLY_NOTICE: "이 문구는 라이더에게만 보이며 고객 앱에는 노출되지 않습니다.",
  GOOD_EXAMPLES_TITLE: "작성 가이드",
  BAD_EXAMPLES_TITLE: "등록할 수 없는 문구",
  MODERATION_NOTICE: "안내 목적과 맞지 않는 문구는 수정 요청 또는 삭제될 수 있습니다.",
  UNSET_LABEL: "미설정",

  PICKUP_TITLE: "라이더 픽업 위치",
  PICKUP_DESCRIPTION:
    "라이더가 방문할 픽업 지점을 가게 실주소와 별도로 등록합니다. 배달 범위·배달팁에는 영향을 주지 않습니다.",
  PICKUP_FALLBACK_LABEL: "가게 실주소 사용",
  PICKUP_SHOP_ADDRESS_PREFIX: "현재 가게 주소:",
  PICKUP_COPY_SHOP_ADDRESS: "가게 실주소와 동일하게 설정",
  PICKUP_COPY_GUIDE: "현재 가게 주소를 픽업 위치로 복사합니다. 이후 가게 주소가 바뀌어도 픽업 위치는 유지됩니다.",
  PICKUP_CLEAR_ACTION: "초기화",
  PICKUP_CLEAR_CONFIRM_TITLE: "픽업 위치를 초기화할까요?",
  PICKUP_CLEAR_CONFIRM_DESCRIPTION:
    "초기화하면 라이더에게 가게 실주소가 픽업 위치로 안내됩니다. 문구는 그대로 유지됩니다.",
  PICKUP_ROAD_ADDRESS_LABEL: "도로명주소",
  PICKUP_LOT_ADDRESS_LABEL: "지번주소",
  PICKUP_DETAIL_ADDRESS_LABEL: "상세주소",
  PICKUP_DETAIL_ADDRESS_PLACEHOLDER: "예) 지하 1층 후문",
  PICKUP_LATITUDE_LABEL: "위도",
  PICKUP_LONGITUDE_LABEL: "경도",
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
  MIN_ORDER_AMOUNT_UPDATE_SUCCESS: "최소주문금액이 저장되었습니다.",
  SCHEDULED_ORDER_UPDATE_SUCCESS: "예약주문 설정이 저장되었습니다.",
  DELIVERY_TIP_UPDATE_SUCCESS: "배달팁이 저장되었습니다.",
  EXTRA_DELIVERY_TIP_UPDATE_SUCCESS: "추가 배달팁이 저장되었습니다.",
  DELIVERY_TIP_REMOVE_SUCCESS: "배달팁 설정이 해제되었습니다.",
  DELIVERY_AREA_CREATE_SUCCESS: "배달가능지역이 등록되었습니다.",
  DELIVERY_AREA_DELETE_SUCCESS: "배달가능지역이 삭제되었습니다.",
  DELIVERY_AREA_SAVE_SUCCESS: "배달지역을 저장했습니다.",
  DELIVERY_AREA_POLYGON_DELETE_SUCCESS: "지도로 그린 배달지역을 해제했습니다.",
  DELIVERY_AREA_ADJUSTMENT_REQUEST_SUCCESS: "배달지역 조정 신청이 접수되었습니다. 가맹본부에 전달됩니다.",
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
  RIDER_VISIT_GUIDE_UPDATE_SUCCESS: "라이더 가게방문 안내가 저장되었습니다.",
  RIDER_VISIT_GUIDE_DELETE_SUCCESS: "라이더 가게방문 안내가 삭제되었습니다.",
  RIDER_PICKUP_LOCATION_UPDATE_SUCCESS: "라이더 픽업 위치가 저장되었습니다.",
  RIDER_PICKUP_LOCATION_CLEAR_SUCCESS: "라이더 픽업 위치가 초기화되었습니다. 가게 실주소가 안내됩니다.",

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
  CONSENT_FILE_REQUIRED: "정보제공 동의서를 첨부해 주세요.",
  CONSENT_FILE_TYPE: "동의서는 jpg, png, gif, webp, pdf 파일만 첨부할 수 있습니다.",
  CONSENT_FILE_SIZE: "동의서 파일 크기는 10MB를 초과할 수 없습니다.",
  ADMIN_DONG_SEARCH_FAILED: "행정동을 검색하지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DELIVERY_AREA_ADJUSTMENTS_LOAD_FAILED: "조정 신청 이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DELIVERY_AREA_MAX_REACHED: "배달가능지역은 최대 500건까지 등록할 수 있습니다.",

  // 배달지역 지도 편집
  DELIVERY_AREA_LOAD_FAILED: "배달지역 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DELIVERY_AREA_RADIUS_APPLIED: "반경 안의 지역을 추가했습니다.",
  DELIVERY_AREA_RADIUS_PREVIEW_PENDING: "반경 안의 지역을 확인하는 중입니다. 잠시 후 다시 시도해 주세요.",
  DELIVERY_AREA_RADIUS_EXCEEDED: "배달지역은 가게 주소 기준 7km를 넘을 수 없습니다.",
  DELIVERY_AREA_EMPTY_PROJECTION: "선택한 영역에 포함되는 행정동이 없습니다. 영역을 더 넓게 그려 주세요.",
  DELIVERY_AREA_COUNT_EXCEEDED: "배달가능지역은 최대 500개까지 등록할 수 있습니다.",
  DELIVERY_AREA_BLOCKED_BY_TIP: "지역별 배달팁이 설정된 지역이 있어 저장할 수 없습니다",
  DELIVERY_AREA_BOUNDARY_LOAD_FAILED: "지역 경계를 불러오지 못했습니다.",
  DELIVERY_AREA_TREE_LOAD_FAILED: "지역 목록을 불러오지 못했습니다.",
  DELIVERY_AREA_DRAFT_RESTORE_PROMPT: "이전에 편집하던 내용이 있습니다. 이어서 하시겠습니까?",
  DELIVERY_AREA_DISCARD_CONFIRM: "저장하지 않은 변경 내용이 사라집니다. 나가시겠습니까?",
  DELIVERY_AREA_NO_CHANGES: "변경된 내용이 없습니다.",
  DELIVERY_AREA_FOCUS_MAP_UNAVAILABLE: "지도를 사용할 수 없어 해당 지역으로 이동할 수 없습니다.",
  DELIVERY_AREA_FOCUS_UNRESOLVED: "이 지역의 위치를 알 수 없습니다. 지도를 그 지역 쪽으로 옮긴 뒤 다시 시도해 주세요.",
} as const;

/** 변경이력 조회 화면 문구 */
export const SHOP_CHANGE_HISTORY_COPY = {
  TITLE: "변경이력 조회",
  DESCRIPTION: "최근 6개월 이내의 변경이력을 확인하실 수 있습니다.",

  // 진입 링크 (운영정보 탭)
  ENTRY_TITLE: "변경이력 조회",
  ENTRY_DESCRIPTION: "가게 설정을 언제 무엇으로 바꿨는지 최근 6개월치를 확인합니다.",
  ENTRY_SUMMARY: "최근 6개월",
  ENTRY_ACTION: "조회",

  // 필터
  CATEGORY_LABEL: "대분류",
  CHANGE_TYPE_LABEL: "중분류",
  DATE_LABEL: "변경일",
  FILTER_ALL: "전체",
  SEARCH: "조회",

  // 목록
  BEFORE_LABEL: "변경 전",
  AFTER_LABEL: "변경 후",
  /** CREATE 의 변경 전 / DELETE 의 변경 후 자리 표시 */
  VALUE_ABSENT: "—",

  // 빈 상태
  SHOP_EMPTY_TITLE: "등록된 가게가 없습니다.",
  SHOP_EMPTY_DESCRIPTION: "가게가 등록되면 이 화면에서 변경이력을 확인할 수 있습니다.",
  LIST_EMPTY_TITLE: "해당 조건의 변경이력이 없습니다.",
  LIST_EMPTY_DESCRIPTION: "다른 날짜나 분류로 조회해 보세요.",

  // 에러
  LIST_LOAD_FAILED: "변경이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  CATALOG_LOAD_FAILED: "변경이력 분류를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DATE_OUT_OF_RANGE: "조회 가능한 기간은 최근 6개월입니다.",
  SHOP_ACCESS_DENIED: "접근 권한이 없는 가게입니다.",
  SHOP_NOT_FOUND: "가게를 찾을 수 없습니다.",
  /** URL 의 shopId 가 내 가게 목록에 없어 첫 가게로 대체했을 때 — 조용히 바뀌면 다른 가게를 보고 있는 줄 모른다 */
  SHOP_FALLBACK_NOTICE: "요청하신 가게를 찾을 수 없어 다른 가게의 변경이력을 표시합니다.",

  // 페이지네이션
  PREVIOUS_PAGE: "이전",
  NEXT_PAGE: "다음",
} as const;

/**
 * 요청처리 현황 조회 화면 문구.
 *
 * 상태·유형 라벨은 여기에 두지 않는다 — 서버 카탈로그가 내려주는 한국어 라벨
 * (`statusDescription`/`requestTypeDescription`)을 그대로 쓴다(`feature/shop/AGENTS.md`).
 */
export const SHOP_REQUEST_COPY = {
  ENTRY_TITLE: "요청처리 현황",
  TITLE: "요청처리 현황",
  DESCRIPTION: "신청한 요청 건의 처리 상태를 확인할 수 있습니다.",

  // 필터
  FILTER_TYPE_LABEL: "요청 유형",
  FILTER_STATUS_LABEL: "처리 상태",
  FILTER_PERIOD_LABEL: "신청 기간",
  FILTER_ALL: "전체",
  FILTER_RESET: "초기화",

  // 빈 상태
  EMPTY_TITLE: "요청 내역이 없습니다",
  EMPTY_DESCRIPTION: "상표·대표이미지 변경이나 배달지역 조정을 신청하면 이곳에서 진행 상황을 확인할 수 있습니다.",
  NO_SHOP_TITLE: "관리할 가게가 없습니다",
  NO_SHOP_DESCRIPTION: "가게가 등록되면 요청처리 현황을 확인할 수 있습니다.",

  // 에러
  LOAD_FAILED: "요청 내역을 불러오지 못했습니다.",
  DATE_RANGE_INVALID: "조회 시작일이 종료일보다 늦습니다.",
  CATALOG_LOAD_FAILED: "요청 유형 정보를 불러오지 못했습니다.",
  DETAIL_LOAD_FAILED: "요청 상세를 불러오지 못했습니다.",
  /** URL 의 shopId 가 소유하지 않은 가게일 때 (서버 403) */
  SHOP_ACCESS_DENIED: "접근 권한이 없는 가게입니다.",
  /** URL 의 shopId 가 존재하지 않는 가게일 때 (서버 404) */
  SHOP_NOT_FOUND: "가게를 찾을 수 없습니다.",

  // 목록·상세 항목
  REQUESTED_AT: "신청일",
  PROCESSED_AT: "처리일",
  REJECT_REASON: "반려 사유",
  CONTRACT_AMENDING: "계약서 수정 대상",
  ATTACHMENT: "첨부 파일",
  COMMENT_COUNT: "문의",
  DETAIL_ACTION: "상세 보기",
  CONTRACT_AMENDING_YES: "예",
  CONTRACT_AMENDING_NO: "아니오",

  // 유형별 상세 블록
  IMAGE_CHANGE_TITLE: "요청 이미지",
  ADJUSTMENT_TITLE: "조정 신청 정보",
  ADJUSTMENT_COUNTERPART_SHOP: "상대 가게",
  ADJUSTMENT_BUSINESS_NUMBER: "사업자등록번호",
  ADJUSTMENT_FRANCHISE: "가맹본부",
  ADJUSTMENT_REASON: "신청 사유",
  ADJUSTMENT_CONSENT_FILE: "정보제공 동의서",

  // 취소
  CANCEL_ACTION: "요청 취소",
  CANCEL_CONFIRM_TITLE: "요청을 취소할까요?",
  CANCEL_CONFIRM_DESCRIPTION: "취소한 요청은 되돌릴 수 없습니다. 필요하면 다시 신청할 수 있습니다.",
  CANCEL_CONFIRM_ACTION: "취소하기",
  CANCEL_CONFIRM_DISMISS: "닫기",
  CANCEL_SUCCESS: "요청을 취소했습니다.",
  CANCEL_FAILED: "요청을 취소하지 못했습니다.",
  CANCEL_NOT_ALLOWED: "대기중인 요청만 취소할 수 있습니다.",

  // 문의 스레드
  COMMENT_SECTION_TITLE: "문의",
  COMMENT_EMPTY: "아직 문의가 없습니다. 궁금한 점을 남기면 담당자가 답변합니다.",
  COMMENT_PLACEHOLDER: "궁금한 점을 남겨주세요.",
  COMMENT_SUBMIT: "문의 등록",
  COMMENT_SUCCESS: "문의를 등록했습니다.",
  COMMENT_FAILED: "문의를 등록하지 못했습니다.",
  COMMENT_REQUIRED: "문의 내용을 입력해주세요.",
  COMMENT_MAX_LENGTH: "문의 내용은 1000자 이내로 입력해주세요.",
  COMMENT_LOAD_FAILED: "문의 내역을 불러오지 못했습니다.",

  // 페이지네이션
  PREVIOUS_PAGE: "이전",
  NEXT_PAGE: "다음",

  /** null 값 자리 표시 */
  VALUE_ABSENT: "—",
} as const;

/** 가게 라우트 error.tsx 공용 문구 */
export const SHOP_ERROR_PAGE_COPY = {
  TITLE: "문제가 발생했습니다",
  RETRY: "다시 시도",
} as const;
