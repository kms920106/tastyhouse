import {
  CUP_COUNT_MAX,
  CUP_COUNT_MIN,
  OPTION_GROUP_DESCRIPTION_MAX_LENGTH,
  OPTION_GROUP_NAME_MAX_LENGTH,
  OPTION_NAME_MAX_LENGTH,
  PRODUCT_CATEGORY_DESCRIPTION_MAX_LENGTH,
  PRODUCT_CATEGORY_NAME_MAX_LENGTH,
  PRODUCT_COMPOSITION_MAX_LENGTH,
  PRODUCT_DESCRIPTION_MAX_LENGTH,
  PRODUCT_NAME_MAX_LENGTH,
  SOLD_OUT_UNTIL_MAX_DAYS,
  SOLD_OUT_UNTIL_MIN_MINUTES,
  VEGETARIAN_DESCRIPTION_MAX_LENGTH,
  VEGETARIAN_INGREDIENTS_MAX_LENGTH,
} from "./constants";

/**
 * 점주 품절·숨김 한국어 문구.
 *
 * 컴포넌트·액션에 문자열을 인라인하지 않는다(`frontend/ceo/CLAUDE.md`).
 */
export const PRODUCT_AVAILABILITY_COPY = {
  ENTRY_TITLE: "품절·숨김",
  PAGE_TITLE: "품절·숨김 설정",
  PAGE_DESCRIPTION: "재료가 떨어졌을 때 메뉴와 옵션을 한 번에 찾아 품절·숨김을 설정할 수 있습니다.",

  SEARCH_PLACEHOLDER: "메뉴·옵션 검색",
  SEARCH_SUBMIT: "검색",
  TAB_MENU: "메뉴",
  TAB_OPTION: "옵션",
  FILTER_SOLD_OUT: "품절보기",
  FILTER_HIDDEN: "숨김보기",
  SELECT_ALL: "전체 선택",

  BULK_PREFIX: "선택한 항목",
  BULK_SUFFIX: "개를",
  BUTTON_HIDE: "숨김",
  BUTTON_SOLD_OUT: "품절",
  BUTTON_RELEASE: "해제",
  BUTTON_RELEASE_SOLD_OUT: "품절 해제",
  BUTTON_RELEASE_HIDDEN: "숨김 해제",
  BUTTON_RELEASE_ALL: "품절·숨김 해제",
  BUTTON_CHANGE_PERIOD: "기간변경",
  BUTTON_SET_PERIOD: "기간설정",

  DIALOG_PERIOD_TITLE: "품절기간 변경",
  DIALOG_PERIOD_DESCRIPTION: `선택한 항목의 품절 종료 시각을 지정합니다. 지금부터 ${SOLD_OUT_UNTIL_MIN_MINUTES}분 이후 ~ ${SOLD_OUT_UNTIL_MAX_DAYS}일 이내로 설정할 수 있습니다.`,
  DIALOG_PERIOD_DATE_LABEL: "날짜",
  DIALOG_PERIOD_HOUR_LABEL: "시",
  DIALOG_PERIOD_MINUTE_LABEL: "분",
  DIALOG_PERIOD_APPLY: "적용하기",
  DIALOG_PERIOD_CANCEL: "취소",

  BADGE_SOLD_OUT: "품절",
  BADGE_HIDDEN: "숨김",
  BADGE_REQUIRED: "필수",
  BADGE_REPRESENTATIVE: "사장님 추천",
  BADGE_COMMON_OPTION: "공통",
  LINKED_PRODUCTS: "연결된 메뉴",
  OPTION_SELECT_RANGE_MIN: "최소",
  OPTION_SELECT_RANGE_MAX: "최대",
  OPTION_SELECT_RANGE_UNIT: "개",
  /** 개수 단위. 옵션 선택 범위(`최소 N개`)와 의미가 달라 상수를 나눈다 */
  COUNT_UNIT: "개",
  ADDITIONAL_PRICE_PREFIX: "+",
  NO_CATEGORY: "분류 없음",

  SOLD_OUT_UNTIL_SUFFIX: "까지 품절",
  SOLD_OUT_INDEFINITE: "기간이 지정되지 않은 품절입니다.",

  FAILURE_NOTICE_TITLE: "처리되지 않은 항목",
  FAILURE_NOTICE_DESCRIPTION: "아래 항목은 조건에 맞지 않아 처리되지 않았습니다. 나머지 항목은 정상 적용되었습니다.",
  FAILURE_NOTICE_DISMISS: "닫기",

  EMPTY: "등록된 메뉴가 없습니다.",
  EMPTY_OPTION: "등록된 옵션이 없습니다.",
  EMPTY_FILTERED: "조건에 맞는 항목이 없습니다.",
  EMPTY_DESCRIPTION: "검색어나 필터를 바꿔 다시 확인해 주세요.",
} as const;

export const PRODUCT_MESSAGE = {
  INVALID_INPUT: "입력값을 다시 확인해 주세요.",
  TARGET_REQUIRED: "대상을 1개 이상 선택해 주세요.",

  SOLD_OUT_SUCCESS: "품절 처리했습니다.",
  HIDE_SUCCESS: "숨김 처리했습니다.",
  RELEASE_SUCCESS: "해제했습니다.",
  PERIOD_CHANGE_SUCCESS: "품절 기간을 변경했습니다.",

  SOLD_OUT_FAILED: "품절 처리에 실패했습니다.",
  HIDE_FAILED: "숨김 처리에 실패했습니다.",
  RELEASE_FAILED: "해제에 실패했습니다.",
  PERIOD_CHANGE_FAILED: "품절 기간을 변경하지 못했습니다.",

  PARTIAL_FAILURE: "일부 항목은 처리되지 않았습니다.",
  /** 부분실패 토스트 제목. 문구를 컴포넌트에 인라인하지 않기 위해 함수 상수로 둔다 */
  PARTIAL_FAILURE_SUMMARY: (succeeded: number, failed: number) => `${succeeded}개 처리, ${failed}개 실패`,
  CHANGE_FAILED: "처리에 실패했습니다.",

  SHOP_LIST_LOAD_FAILED: "내 가게 목록을 불러오지 못했습니다.",
  LOAD_FAILED: "목록을 불러오지 못했습니다.",
  SHOP_ACCESS_DENIED: "이 가게에 접근할 권한이 없습니다.",
  SHOP_NOT_FOUND: "가게를 찾을 수 없습니다.",
  SHOP_EMPTY: "등록된 가게가 없습니다.",
} as const;

/** 폼 검증 문구. 서버도 같은 규칙으로 400 을 내므로 문구를 맞춘다 */
export const PRODUCT_VALIDATION_MESSAGE = {
  SOLD_OUT_UNTIL_DATE_REQUIRED: "날짜를 선택해 주세요.",
  SOLD_OUT_UNTIL_HOUR_REQUIRED: "시를 선택해 주세요.",
  SOLD_OUT_UNTIL_MINUTE_REQUIRED: "분을 선택해 주세요.",
  SOLD_OUT_UNTIL_INVALID: "올바른 일시를 선택해 주세요.",
  SOLD_OUT_UNTIL_TOO_SOON: `품절 기간은 현재 시각으로부터 ${SOLD_OUT_UNTIL_MIN_MINUTES}분 이후여야 합니다.`,
  SOLD_OUT_UNTIL_TOO_FAR: `품절 기간은 최대 ${SOLD_OUT_UNTIL_MAX_DAYS}일 이내여야 합니다.`,
  SHOP_REQUIRED: "가게를 선택해 주세요.",
} as const;

// =====================================================================================
// 점주 메뉴·옵션 관리 문구 (`docs/tasks/frontend.md`)
// =====================================================================================

export const PRODUCT_MENU_COPY = {
  ENTRY_TITLE: "메뉴 관리",
  PAGE_TITLE: "메뉴판 관리",
  PAGE_DESCRIPTION: "메뉴그룹과 메뉴를 추가하고 손님에게 보이는 순서를 끌어서 바꿀 수 있습니다.",

  BUTTON_ADD_MENU: "메뉴 추가",
  BUTTON_ADD_GROUP: "메뉴그룹 추가",
  BUTTON_MANAGE_OPTION_GROUPS: "옵션그룹 관리",
  BUTTON_EDIT: "수정",
  BUTTON_DELETE: "삭제",
  BUTTON_CANCEL: "취소",
  BUTTON_SAVE: "저장",
  BUTTON_CONFIRM_DELETE: "삭제하기",
  BUTTON_DETAIL: "상세",

  DRAG_HANDLE_LABEL: "순서 변경 손잡이",
  GROUP_MENU_COUNT_SUFFIX: "개",

  DIALOG_MENU_CREATE_TITLE: "메뉴 추가",
  DIALOG_MENU_CREATE_DESCRIPTION: "메뉴명과 가격은 필수입니다. 이미지·채식 설정은 등록 후 상세 화면에서 요청합니다.",
  DIALOG_GROUP_CREATE_TITLE: "메뉴그룹 추가",
  DIALOG_GROUP_EDIT_TITLE: "메뉴그룹 수정",
  DIALOG_GROUP_DESCRIPTION: "같은 성격의 메뉴를 묶는 이름입니다. 손님 화면에서 이 이름으로 묶여 보입니다.",
  DIALOG_MENU_DELETE_TITLE: "메뉴 삭제",
  DIALOG_MENU_DELETE_DESCRIPTION:
    "삭제한 메뉴는 되돌릴 수 없습니다. 지난 주문·리뷰의 메뉴 정보는 주문 시점 기록이 남아 영향받지 않습니다.",
  DIALOG_GROUP_DELETE_TITLE: "메뉴그룹 삭제",
  DIALOG_GROUP_DELETE_DESCRIPTION: "소속된 메뉴가 있으면 삭제할 수 없습니다. 먼저 메뉴를 다른 그룹으로 옮겨 주세요.",

  FIELD_NAME: "메뉴명",
  FIELD_CATEGORY: "메뉴그룹",
  FIELD_COMPOSITION: "메뉴구성",
  FIELD_DESCRIPTION: "메뉴설명",
  FIELD_ORIGINAL_PRICE: "정가",
  FIELD_DISCOUNT_PRICE: "할인가",
  FIELD_SINGLE_SERVING: "1인분 메뉴",
  FIELD_REPRESENTATIVE: "사장님 추천",
  FIELD_SPICINESS: "매운맛 단계",
  FIELD_RATING_EXCLUDED: "메뉴 평가 제외",
  FIELD_GROUP_NAME: "메뉴그룹명",
  FIELD_GROUP_DESCRIPTION: "메뉴그룹 설명",

  HELP_NAME: "한글·영문·숫자와 : , . / ~ % & ( ) + [ ] ™ ® 만 쓸 수 있습니다.",
  HELP_COMPOSITION: "구성품을 적으면 목록에서 메뉴명 아래에 보입니다.",
  HELP_DISCOUNT_PRICE: "정가보다 클 수 없습니다. 비워 두면 할인 없이 정가로 판매합니다.",
  HELP_SINGLE_SERVING: "1인분 주문이 가능한 메뉴에 표시됩니다.",
  HELP_REPRESENTATIVE: "메뉴판 상단에 추천으로 노출됩니다. 최소 1개는 남아 있어야 합니다.",
  HELP_RATING_EXCLUDED: "주류·사이드처럼 맛 평가가 어울리지 않는 메뉴는 평가 대상에서 제외합니다.",

  PLACEHOLDER_CATEGORY_NONE: "분류 없음",
  PLACEHOLDER_SELECT: "선택해 주세요",

  EMPTY_GROUPS: "등록된 메뉴그룹이 없습니다.",
  EMPTY_GROUPS_DESCRIPTION: "메뉴그룹을 먼저 추가하면 메뉴를 묶어 관리할 수 있습니다.",
  EMPTY_MENUS_IN_GROUP: "이 그룹에 메뉴가 없습니다.",

  BULK_PREFIX: "선택한 메뉴",
  BULK_SUFFIX: "개를",

  SORT_STALE_TITLE: "목록이 변경되었습니다.",
  SORT_STALE_DESCRIPTION: "새로고침 후 다시 시도해 주세요.",
} as const;

export const PRODUCT_DETAIL_COPY = {
  PAGE_TITLE: "메뉴 상세",
  PAGE_DESCRIPTION: "각 항목의 [변경] 버튼을 눌러 수정합니다. 이미지와 채식 설정은 관리자 검수를 거칩니다.",
  BUTTON_BACK: "메뉴판으로",

  ROW_NAME: "메뉴명",
  ROW_TEXT: "메뉴구성·설명",
  ROW_PRICE: "가격",
  ROW_FLAGS: "판매 옵션",
  ROW_CATEGORY: "메뉴그룹",
  ROW_EXPOSURE: "노출기간",
  ROW_IMAGE: "메뉴 이미지",
  ROW_VEGETARIAN: "채식 설정",
  ROW_OPTION_GROUPS: "옵션그룹",

  SHEET_NAME_TITLE: "메뉴명 변경",
  SHEET_TEXT_TITLE: "메뉴구성·설명 변경",
  SHEET_PRICE_TITLE: "가격 변경",
  SHEET_CATEGORY_TITLE: "메뉴그룹 변경",
  SHEET_EXPOSURE_TITLE: "노출기간 설정",
  SHEET_IMAGE_TITLE: "메뉴 이미지 설정",
  SHEET_VEGETARIAN_TITLE: "채식 설정",
  SHEET_OPTION_GROUP_TITLE: "옵션그룹 연결",

  // 노출기간
  EXPOSURE_ALWAYS_LABEL: "상시 노출",
  EXPOSURE_ALWAYS_DESCRIPTION: "끄면 기간·요일·시간대를 지정할 수 있습니다.",
  EXPOSURE_PERIOD_LABEL: "노출 기간",
  EXPOSURE_PERIOD_HELP: "둘 다 비우면 기간 제한 없이 노출됩니다. 종료일은 그날까지 포함합니다.",
  EXPOSURE_DAY_MODE_LABEL: "요일 지정 방식",
  EXPOSURE_DAY_MODE_PRESET: "묶음으로 고르기",
  EXPOSURE_DAY_MODE_INDIVIDUAL: "요일별로 고르기",
  EXPOSURE_DAY_MODE_HELP: "묶음과 개별 요일은 함께 쓸 수 없어 방식을 먼저 고릅니다.",
  EXPOSURE_TIME_LABEL: "노출 시간대",
  EXPOSURE_TIME_HELP: "비우면 해당 요일 종일 노출됩니다.",
  EXPOSURE_TIME_START: "시작",
  EXPOSURE_TIME_END: "종료",
  EXPOSURE_TIME_ALL_DAY: "종일",
  EXPOSURE_OVERNIGHT_NOTICE: "종료 시각이 시작보다 빨라 다음 날 새벽까지 이어지는 것으로 처리됩니다.",
  EXPOSURE_BADGE_EXPOSED: "지금 노출 중",
  EXPOSURE_SUMMARY_ALWAYS: "상시 노출",

  // 이미지
  IMAGE_SPEC_HELP: "최소 1280×960px, 15MB 이하 JPG·PNG 파일만 등록할 수 있습니다.",
  IMAGE_APPROVAL_NOTICE: "새 이미지는 관리자 검수를 거친 뒤 손님 화면에 반영됩니다.",
  IMAGE_UPLOAD_LABEL: "이미지 파일 선택",
  IMAGE_UPLOAD_SUBMIT: "등록 요청",
  IMAGE_SORT_HELP: "순서 변경과 삭제는 검수 없이 바로 반영됩니다.",
  IMAGE_EMPTY: "등록된 이미지가 없습니다.",
  IMAGE_DELETE: "이미지 삭제",

  // 채식
  VEGETARIAN_ENABLE_LABEL: "채식 메뉴로 설정",
  VEGETARIAN_TYPE_LABEL: "채식 단계",
  VEGETARIAN_INGREDIENTS_LABEL: "재료",
  VEGETARIAN_INGREDIENTS_HELP: "사용한 재료를 빠짐없이 적어 주세요. 검수에 쓰입니다.",
  VEGETARIAN_DESCRIPTION_LABEL: "메뉴 설명",
  VEGETARIAN_APPROVAL_NOTICE: "채식 설정은 관리자 검수를 거친 뒤 반영됩니다.",
  VEGETARIAN_NOT_ALLOWED: "이 가게의 카테고리에서는 채식 메뉴를 설정할 수 없습니다.",
  VEGETARIAN_SUBMIT: "설정 요청",
  VEGETARIAN_CLEAR: "채식 설정 해제",

  // 옵션그룹 연결
  OPTION_GROUP_LINK: "옵션그룹 연결",
  OPTION_GROUP_UNLINK: "연결 해제",
  OPTION_GROUP_EMPTY: "연결된 옵션그룹이 없습니다.",
  OPTION_GROUP_LINKED_COUNT_PREFIX: "다른 메뉴 ",
  OPTION_GROUP_LINKED_COUNT_SUFFIX: "개에서도 사용 중입니다.",
  OPTION_GROUP_LAST_LINK_NOTICE:
    "이 옵션그룹의 마지막 연결입니다. 옵션그룹을 삭제하려면 옵션그룹 관리에서 진행해 주세요.",
  OPTION_GROUP_LINK_DIALOG_TITLE: "연결할 옵션그룹 선택",
  OPTION_GROUP_LINK_DIALOG_DESCRIPTION: "같은 가게의 옵션그룹만 연결할 수 있습니다.",
  OPTION_GROUP_LINK_EMPTY: "연결할 수 있는 옵션그룹이 없습니다.",

  BADGE_PENDING: "검수 중",
  BADGE_REJECTED: "반려됨",
  BADGE_APPROVED: "승인됨",
  REJECT_REASON_PREFIX: "반려 사유: ",

  NOT_SET: "설정 안 함",
  LOAD_DETAIL_FAILED: "메뉴 정보를 불러오지 못했습니다.",
} as const;

export const PRODUCT_OPTION_GROUP_COPY = {
  PAGE_TITLE: "옵션그룹 관리",
  PAGE_DESCRIPTION: "옵션그룹은 여러 메뉴에 연결할 수 있습니다. 여기서 만든 그룹을 메뉴 상세에서 연결합니다.",

  BUTTON_ADD_GROUP: "옵션그룹 추가",
  BUTTON_ADD_OPTION: "옵션 추가",
  BUTTON_EDIT_GROUP: "그룹 수정",
  BUTTON_DELETE_GROUP: "그룹 삭제",

  DIALOG_GROUP_CREATE_TITLE: "옵션그룹 추가",
  DIALOG_GROUP_EDIT_TITLE: "옵션그룹 수정",
  DIALOG_OPTION_CREATE_TITLE: "옵션 추가",
  DIALOG_OPTION_EDIT_TITLE: "옵션 수정",
  DIALOG_GROUP_DELETE_TITLE: "옵션그룹 삭제",
  DIALOG_GROUP_DELETE_DESCRIPTION: "이 그룹과 소속 옵션이 모두 삭제되고 연결된 메뉴에서도 사라집니다.",
  DIALOG_OPTION_DELETE_TITLE: "옵션 삭제",
  DIALOG_OPTION_DELETE_DESCRIPTION: "그룹의 최소 선택 개수를 채우지 못하게 되면 삭제할 수 없습니다.",

  FIELD_LINK_PRODUCT: "연결할 메뉴",
  HELP_LINK_PRODUCT:
    "옵션그룹은 등록 시 이 메뉴에 곧바로 연결됩니다. 다른 메뉴에는 나중에 메뉴 상세에서 연결할 수 있습니다.",
  FIELD_GROUP_NAME: "옵션그룹명",
  FIELD_GROUP_DESCRIPTION: "설명",
  FIELD_REQUIRED: "필수 선택",
  FIELD_MULTIPLE_SELECT: "다중 선택",
  FIELD_MIN_SELECT: "최소 선택 개수",
  FIELD_MAX_SELECT: "최대 선택 개수",
  FIELD_OPTION_NAME: "옵션명",
  FIELD_ADDITIONAL_PRICE: "추가 금액",

  HELP_REQUIRED: "켜면 손님이 반드시 하나 이상 골라야 주문할 수 있습니다.",
  HELP_MULTIPLE_SELECT: "켜면 여러 개를 함께 고를 수 있습니다.",
  HELP_SELECT_RANGE: "다중 선택일 때만 의미가 있습니다. 최소는 최대보다 클 수 없습니다.",

  LINKED_PRODUCTS_LABEL: "연결된 메뉴",
  LINKED_COUNT_SUFFIX: "개 메뉴에 연결",
  EMPTY_GROUPS: "등록된 옵션그룹이 없습니다.",
  EMPTY_GROUPS_DESCRIPTION: "옵션그룹을 추가한 뒤 메뉴 상세에서 연결해 주세요.",
  EMPTY_OPTIONS: "이 그룹에 옵션이 없습니다.",

  // ===== 일회용컵 보증금 (`frontend.md` §3) =====

  FIELD_GROUP_TYPE: "옵션그룹 유형",
  HELP_GROUP_TYPE: "유형은 등록할 때만 정할 수 있고 나중에 바꿀 수 없습니다.",
  BADGE_CUP_DEPOSIT: "일회용컵 보증금",
  /** 보증금 그룹은 필수 선택 불가 + 최대 1개 고정이라 폼에서 미리 알린다(서버가 강제한다) */
  NOTICE_CUP_DEPOSIT_GROUP:
    "일회용컵 보증금 옵션그룹은 필수 선택으로 설정할 수 없고, 선택 개수는 최대 1개로 고정됩니다.",

  FIELD_CUP_COUNT: "일회용컵 제공 개수",
  HELP_CUP_COUNT: "이 옵션에서 보증금이 부과되어야 하는 음료의 개수입니다. 예: 옵션명이 `아메리카노+라떼` 이면 2개.",
  FIELD_PERSONAL_CUP: "개인컵 사용 옵션",
  HELP_PERSONAL_CUP: "켜면 보증금 대신 할인 금액을 설정합니다. 개인컵 옵션에는 컵 개수를 입력하지 않습니다.",
  FIELD_PERSONAL_CUP_DISCOUNT: "개인컵 할인 금액",
  NOTICE_DEPOSIT_ADDITIONAL_PRICE: "보증금 옵션에는 추가 금액을 설정할 수 없습니다.",
  /** 폼 미리보기. 최종 금액은 서버가 확정한다 */
  DEPOSIT_PREVIEW: (amount: number, perCup: number) =>
    `보증금 ${amount.toLocaleString("ko-KR")}원 (컵 1개당 ${perCup}원)`,
  OPTION_DEPOSIT_SUMMARY: (cupCount: number, depositAmount: number) =>
    `컵 ${cupCount}개 · 보증금 ${depositAmount.toLocaleString("ko-KR")}원`,
  OPTION_PERSONAL_CUP_SUMMARY: (discountAmount: number) => `개인컵 할인 ${discountAmount.toLocaleString("ko-KR")}원`,
} as const;

/** 메뉴·옵션 관리 액션 결과 문구 */
export const PRODUCT_MENU_MESSAGE = {
  MENU_CREATE_SUCCESS: "메뉴를 등록했습니다.",
  MENU_CREATE_FAILED: "메뉴를 등록하지 못했습니다.",
  MENU_UPDATE_SUCCESS: "메뉴 정보를 변경했습니다.",
  MENU_UPDATE_FAILED: "메뉴 정보를 변경하지 못했습니다.",
  MENU_DELETE_SUCCESS: "메뉴를 삭제했습니다.",
  MENU_DELETE_FAILED: "메뉴를 삭제하지 못했습니다.",

  CATEGORY_CREATE_SUCCESS: "메뉴그룹을 추가했습니다.",
  CATEGORY_CREATE_FAILED: "메뉴그룹을 추가하지 못했습니다.",
  CATEGORY_UPDATE_SUCCESS: "메뉴그룹을 변경했습니다.",
  CATEGORY_UPDATE_FAILED: "메뉴그룹을 변경하지 못했습니다.",
  CATEGORY_DELETE_SUCCESS: "메뉴그룹을 삭제했습니다.",
  CATEGORY_DELETE_FAILED: "메뉴그룹을 삭제하지 못했습니다.",

  ORDER_CHANGE_SUCCESS: "순서를 변경했습니다.",
  ORDER_CHANGE_FAILED: "순서를 변경하지 못했습니다.",
  CATEGORY_MOVE_SUCCESS: "메뉴를 옮겼습니다.",
  CATEGORY_MOVE_FAILED: "메뉴를 옮기지 못했습니다.",

  OPTION_GROUP_CREATE_SUCCESS: "옵션그룹을 추가했습니다.",
  OPTION_GROUP_CREATE_FAILED: "옵션그룹을 추가하지 못했습니다.",
  OPTION_GROUP_UPDATE_SUCCESS: "옵션그룹을 변경했습니다.",
  OPTION_GROUP_UPDATE_FAILED: "옵션그룹을 변경하지 못했습니다.",
  OPTION_GROUP_DELETE_SUCCESS: "옵션그룹을 삭제했습니다.",
  OPTION_GROUP_DELETE_FAILED: "옵션그룹을 삭제하지 못했습니다.",

  OPTION_CREATE_SUCCESS: "옵션을 추가했습니다.",
  OPTION_CREATE_FAILED: "옵션을 추가하지 못했습니다.",
  OPTION_UPDATE_SUCCESS: "옵션을 변경했습니다.",
  OPTION_UPDATE_FAILED: "옵션을 변경하지 못했습니다.",
  OPTION_DELETE_SUCCESS: "옵션을 삭제했습니다.",
  OPTION_DELETE_FAILED: "옵션을 삭제하지 못했습니다.",

  OPTION_GROUP_LINK_SUCCESS: "옵션그룹을 연결했습니다.",
  OPTION_GROUP_LINK_FAILED: "옵션그룹을 연결하지 못했습니다.",
  OPTION_GROUP_UNLINK_SUCCESS: "옵션그룹 연결을 해제했습니다.",
  OPTION_GROUP_UNLINK_FAILED: "옵션그룹 연결을 해제하지 못했습니다.",

  EXPOSURE_SAVE_SUCCESS: "노출기간을 저장했습니다.",
  EXPOSURE_SAVE_FAILED: "노출기간을 저장하지 못했습니다.",
  EXPOSURE_CLEAR_SUCCESS: "상시 노출로 변경했습니다.",
  EXPOSURE_CLEAR_FAILED: "상시 노출로 변경하지 못했습니다.",
  EXPOSURE_LOAD_FAILED: "노출기간 설정을 불러오지 못했습니다.",

  IMAGE_REQUEST_SUCCESS: "이미지 등록을 요청했습니다. 검수 후 반영됩니다.",
  IMAGE_REQUEST_FAILED: "이미지 등록을 요청하지 못했습니다.",
  IMAGE_ORDER_SUCCESS: "이미지 순서를 변경했습니다.",
  IMAGE_ORDER_FAILED: "이미지 순서를 변경하지 못했습니다.",
  IMAGE_DELETE_SUCCESS: "이미지를 삭제했습니다.",
  IMAGE_DELETE_FAILED: "이미지를 삭제하지 못했습니다.",
  IMAGE_LOAD_FAILED: "이미지 목록을 불러오지 못했습니다.",

  VEGETARIAN_REQUEST_SUCCESS: "채식 설정을 요청했습니다. 검수 후 반영됩니다.",
  VEGETARIAN_REQUEST_FAILED: "채식 설정을 요청하지 못했습니다.",
  VEGETARIAN_CLEAR_SUCCESS: "채식 설정 해제를 요청했습니다.",
  VEGETARIAN_CLEAR_FAILED: "채식 설정을 해제하지 못했습니다.",
  VEGETARIAN_LOAD_FAILED: "채식 설정을 불러오지 못했습니다.",

  LINKED_PRODUCTS_LOAD_FAILED: "연결된 메뉴를 불러오지 못했습니다.",
  MENU_NOT_FOUND: "메뉴를 찾을 수 없습니다.",
  IMAGE_FILE_REQUIRED: "이미지 파일을 선택해 주세요.",
} as const;

/** 메뉴·옵션 관리 폼 검증 문구 */
export const PRODUCT_MENU_VALIDATION_MESSAGE = {
  NAME_REQUIRED: "메뉴명을 입력해 주세요.",
  NAME_TOO_LONG: `메뉴명은 ${PRODUCT_NAME_MAX_LENGTH}자 이내로 입력해 주세요.`,
  NAME_INVALID_CHARACTER: "메뉴명에 사용할 수 없는 특수문자가 포함되어 있습니다.",
  COMPOSITION_TOO_LONG: `메뉴구성은 ${PRODUCT_COMPOSITION_MAX_LENGTH}자 이내로 입력해 주세요.`,
  DESCRIPTION_TOO_LONG: `메뉴설명은 ${PRODUCT_DESCRIPTION_MAX_LENGTH}자 이내로 입력해 주세요.`,
  ORIGINAL_PRICE_REQUIRED: "정가를 입력해 주세요.",
  PRICE_NEGATIVE: "가격은 0원 이상이어야 합니다.",
  PRICE_NOT_INTEGER: "가격은 원 단위 정수로 입력해 주세요.",
  DISCOUNT_PRICE_EXCEEDS_ORIGINAL: "할인가는 정가보다 클 수 없습니다.",

  CATEGORY_NAME_REQUIRED: "메뉴그룹명을 입력해 주세요.",
  CATEGORY_NAME_TOO_LONG: `메뉴그룹명은 ${PRODUCT_CATEGORY_NAME_MAX_LENGTH}자 이내로 입력해 주세요.`,
  CATEGORY_DESCRIPTION_TOO_LONG: `설명은 ${PRODUCT_CATEGORY_DESCRIPTION_MAX_LENGTH}자 이내로 입력해 주세요.`,

  LINK_PRODUCT_REQUIRED: "연결할 메뉴를 선택해 주세요.",
  OPTION_GROUP_NAME_REQUIRED: "옵션그룹명을 입력해 주세요.",
  OPTION_GROUP_NAME_TOO_LONG: `옵션그룹명은 ${OPTION_GROUP_NAME_MAX_LENGTH}자 이내로 입력해 주세요.`,
  OPTION_GROUP_DESCRIPTION_TOO_LONG: `설명은 ${OPTION_GROUP_DESCRIPTION_MAX_LENGTH}자 이내로 입력해 주세요.`,
  SELECT_COUNT_NEGATIVE: "선택 개수는 0 이상이어야 합니다.",
  MIN_SELECT_EXCEEDS_MAX: "최소 선택 개수는 최대 선택 개수보다 클 수 없습니다.",

  OPTION_NAME_REQUIRED: "옵션명을 입력해 주세요.",
  OPTION_NAME_TOO_LONG: `옵션명은 ${OPTION_NAME_MAX_LENGTH}자 이내로 입력해 주세요.`,
  ADDITIONAL_PRICE_NEGATIVE: "추가 금액은 0원 이상이어야 합니다.",

  EXPOSURE_PERIOD_INVALID: "노출 종료일은 시작일보다 빠를 수 없습니다.",
  EXPOSURE_DAY_REQUIRED: "노출할 요일을 1개 이상 선택해 주세요.",
  EXPOSURE_DAY_TYPE_MIXED: "요일 묶음과 개별 요일은 함께 설정할 수 없습니다.",
  EXPOSURE_TIME_INCOMPLETE: "시작과 종료 시각을 함께 지정해 주세요.",

  VEGETARIAN_TYPE_REQUIRED: "채식 단계를 선택해 주세요.",
  VEGETARIAN_INGREDIENTS_REQUIRED: "재료를 입력해 주세요.",
  VEGETARIAN_INGREDIENTS_TOO_LONG: `재료는 ${VEGETARIAN_INGREDIENTS_MAX_LENGTH}자 이내로 입력해 주세요.`,
  VEGETARIAN_DESCRIPTION_TOO_LONG: `메뉴 설명은 ${VEGETARIAN_DESCRIPTION_MAX_LENGTH}자 이내로 입력해 주세요.`,

  MERGE_TARGET_REQUIRED: "합칠 옵션그룹을 1개 이상 선택해주세요.",
  MERGE_BASE_INCLUDED: "기준 옵션그룹은 합칠 대상에 포함할 수 없습니다.",
  MERGE_SIGNATURE_REQUIRED: "추천 정보가 올바르지 않습니다. 새로고침 후 다시 시도해 주세요.",

  CUP_COUNT_REQUIRED: "일회용컵 제공 개수를 입력해 주세요.",
  CUP_COUNT_RANGE: `일회용컵 제공 개수는 ${CUP_COUNT_MIN}개 이상 ${CUP_COUNT_MAX}개 이하로 입력해 주세요.`,
  CUP_COUNT_NOT_ALLOWED: "일반 옵션에는 일회용컵 제공 개수를 설정할 수 없습니다.",
  DEPOSIT_ADDITIONAL_PRICE_NOT_ALLOWED: "일회용컵 보증금 옵션에는 추가 금액을 설정할 수 없습니다.",
  PERSONAL_CUP_DISCOUNT_REQUIRED: "개인컵 할인 금액을 입력해 주세요.",
  PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP: "개인컵 사용 옵션은 일회용컵 보증금 옵션그룹에만 등록할 수 있습니다.",
  PERSONAL_CUP_DISCOUNT_NOT_ALLOWED: "개인컵 사용 옵션이 아니면 할인 금액을 설정할 수 없습니다.",
  DEPOSIT_GROUP_CANNOT_BE_REQUIRED: "일회용컵 보증금 옵션그룹은 필수 선택으로 설정할 수 없습니다.",
  DEPOSIT_GROUP_SELECT_FIXED: "일회용컵 보증금 옵션그룹의 선택 개수는 변경할 수 없습니다.",
} as const;

/**
 * 옵션그룹 관리 화면(`/dashboard/shop/menus/option-groups`)에서만 쓰는 추가 문구.
 *
 * `PRODUCT_OPTION_GROUP_COPY` 를 직접 늘리지 않고 별도 상수로 둔 이유는, 그 상수를 메뉴 상세의
 * 연결 Sheet 도 함께 참조해 병행 작업에서 충돌하기 쉬운 자리이기 때문이다.
 */
/** 메뉴·옵션 관리 통합 레이아웃의 탭 문구 (`frontend.md` §6-1) */
export const MENU_TAB_COPY = {
  TAB_MENU: "메뉴",
  TAB_OPTION: "옵션",
} as const;

export const OPTION_GROUP_SCREEN_COPY = {
  BUTTON_EDIT_OPTION: "옵션 수정",
  BUTTON_DELETE_OPTION: "옵션 삭제",
  BUTTON_SAVE: "저장",
  BUTTON_CANCEL: "취소",
  BUTTON_DELETE: "삭제",
  DRAG_HANDLE_LABEL: "옵션 순서 변경 손잡이",
  BADGE_REQUIRED: "필수",
  BADGE_MULTIPLE_SELECT: "다중",
  /** 헤더의 `최소~최대` 요약. 한쪽만 있어도 읽히도록 미지정은 `-` 로 채운다 */
  SELECT_RANGE: (min: number | null, max: number | null) => `${min ?? "-"}~${max ?? "-"}개 선택`,
  LINKED_COUNT: (count: number) => `${count}개 메뉴에 연결`,
  OPTION_COUNT: (count: number) => `옵션 ${count}개`,
  SELECT_RANGE_PLACEHOLDER: "미지정",
} as const;

/**
 * 메뉴 상세 화면(`/dashboard/shop/menus/[productId]`)에서만 쓰는 추가 문구.
 *
 * `PRODUCT_DETAIL_COPY` 를 직접 늘리지 않고 별도 상수로 둔 이유는 `OPTION_GROUP_SCREEN_COPY` 와
 * 같다 — 그 상수를 여러 화면이 함께 참조해 병행 작업에서 충돌하기 쉬운 자리다.
 */
export const PRODUCT_DETAIL_SCREEN_COPY = {
  BUTTON_SAVE: "저장",
  BUTTON_CANCEL: "취소",
  BUTTON_SAVING: "저장 중...",
  BUTTON_APPLY: "적용",

  DRAG_HANDLE_LABEL: "이미지 순서 변경 손잡이",
  OPTION_GROUP_DRAG_HANDLE_LABEL: "옵션그룹 순서 변경 손잡이",

  /** 노출 판정 사유(`ProductHiddenReason`) → 배지 문구. 서버가 코드만 내려 화면이 번역한다 */
  EXPOSURE_HIDDEN_REASON_LABEL: {
    MANUALLY_HIDDEN: "숨김 처리됨",
    BEFORE_EXPOSURE_PERIOD: "노출 시작 전",
    AFTER_EXPOSURE_PERIOD: "노출 종료됨",
    OUT_OF_EXPOSURE_HOURS: "노출 시간 아님",
  },

  EXPOSURE_DAY_LABEL: "노출 요일",
  EXPOSURE_DAY_REQUIRED_NOTICE: "요일을 1개 이상 골라야 노출 스케줄이 저장됩니다.",
  EXPOSURE_PERIOD_PLACEHOLDER: "기간 선택",
  EXPOSURE_PERIOD_CLEAR: "기간 비우기",
  EXPOSURE_SUMMARY_SCHEDULED: "노출기간 설정됨",

  IMAGE_PENDING_NOTICE: "이미 검수 대기 중인 요청이 있어 새 이미지를 요청할 수 없습니다.",
  IMAGE_ALT_PREFIX: "메뉴 이미지 ",
  IMAGE_PENDING_ALT: "검수 대기 중인 이미지",
  IMAGE_COUNT_SUFFIX: "장",

  OPTION_GROUP_SELECTED_SUFFIX: "개 선택됨",
  OPTION_GROUP_COUNT_SUFFIX: "개 연결",
  OPTION_GROUP_UNLINK_CONFIRM_TITLE: "옵션그룹 연결 해제",
  OPTION_GROUP_UNLINK_CONFIRM_DESCRIPTION: "이 메뉴에서만 연결이 끊기고 옵션그룹 자체는 남습니다.",
  OPTION_GROUP_ONLY_LINK: "이 메뉴에만 연결되어 있습니다.",

  FLAG_ON: "설정함",
  FLAG_OFF: "설정 안 함",

  /** 상세 조회 실패 시 Sheet 를 열지 않고 이 안내만 남긴다(`frontend.md` §7) */
  DETAIL_UNAVAILABLE_TITLE: "메뉴 정보를 불러오지 못했습니다.",
  DETAIL_UNAVAILABLE_DESCRIPTION: "잠시 후 다시 시도하거나 메뉴판으로 돌아가 주세요.",
} as const;

/**
 * 옵션그룹 합치기 화면(`/dashboard/shop/menus/option-groups/merge`) 전용 문구.
 *
 * `PRODUCT_OPTION_GROUP_COPY` 를 늘리지 않고 별도 상수로 둔 이유는 `OPTION_GROUP_SCREEN_COPY` 와
 * 같다 — 그 상수를 옵션그룹 관리와 메뉴 상세가 함께 참조해 병행 작업에서 충돌하기 쉽다.
 */
export const OPTION_GROUP_MERGE_COPY = {
  PAGE_TITLE: "옵션그룹 합치기",
  PAGE_DESCRIPTION: "이름과 옵션이 같은 옵션그룹을 하나로 모아 관리 부담을 줄입니다.",

  /** 진입 배너. 추천이 0건이면 배너 자체를 렌더링하지 않는다 */
  BANNER_TITLE: (bundleCount: number) => `합칠 수 있는 옵션그룹이 ${bundleCount}묶음 있어요`,
  BANNER_ACTION: "옵션그룹 합치러 가기",

  TAB_RECOMMENDED: "추천 합치기",
  TAB_MANUAL: "직접 선택",

  // ===== 추천 목록 =====

  SUGGESTION_GROUP_COUNT: (count: number) => `${count}개`,
  SUGGESTION_LINKED_COUNT: (count: number) => `이 옵션을 사용하는 메뉴 ${count}개`,
  SUGGESTION_OPTIONS_LABEL: "공통 옵션",
  SUGGESTION_GROUPS_LABEL: "묶음에 포함된 옵션그룹",
  BUTTON_EXCLUDE: "제외",
  BUTTON_MERGE_THIS: "이 묶음 합치기",
  BUTTON_MERGE_THIS_SELECTED: "선택됨 — 아래에서 기준 선택",
  EMPTY_SUGGESTIONS: "합칠 수 있는 옵션그룹이 없습니다.",
  EMPTY_SUGGESTIONS_DESCRIPTION: "이름·옵션 구성이 같은 옵션그룹이 2개 이상일 때 추천이 나타납니다.",

  DIALOG_EXCLUDE_TITLE: "추천 목록에서 제외",
  /** 제외는 영구적이라(append-only exclusion) 되돌릴 수 없음을 반드시 알린다 */
  DIALOG_EXCLUDE_DESCRIPTION: "이 묶음은 추천 목록에서 영구히 제외되며 다시 되돌릴 수 없습니다.",

  // ===== 직접 선택 =====

  SEARCH_PLACEHOLDER: "옵션그룹명 검색",
  SEARCH_SUBMIT: "검색",
  MANUAL_SELECT_LABEL: "합칠 옵션그룹 선택",
  MANUAL_SELECT_HELP: "2개 이상 선택하면 기준 옵션그룹을 고를 수 있습니다.",
  MANUAL_SELECTED_COUNT: (count: number) => `${count}개 선택됨`,
  BASE_SELECT_LABEL: "기준 옵션그룹",
  BASE_SELECT_HELP: "기준으로 고른 옵션그룹만 남고, 나머지는 이 그룹으로 흡수됩니다.",
  EMPTY_GROUPS: "선택할 옵션그룹이 없습니다.",
  EMPTY_SEARCH_RESULT: "검색 결과가 없습니다.",

  // ===== 상세보기 diff =====

  BUTTON_VIEW_DIFF: "상세보기",
  DIFF_SHEET_TITLE: "기준 옵션그룹과의 차이",
  DIFF_SHEET_DESCRIPTION: "기준과 다른 항목을 표시합니다. 흡수될 그룹에만 있는 옵션은 합치면 사라집니다.",
  DIFF_BASE_LABEL: "기준",
  DIFF_CANDIDATE_LABEL: "흡수 대상",
  DIFF_FIELD_DIFFERS: "기준과 다름",
  /** 옵션 단위 diff 배지. `ONLY_IN_CANDIDATE` 가 가장 중요한 경고다 */
  DIFF_TYPE_LABEL: {
    SAME: "동일",
    ONLY_IN_BASE: "기준에만 있음",
    ONLY_IN_CANDIDATE: "합치면 사라짐",
    PRICE_DIFFERS: "가격 다름",
  },
  DIFF_PRICE_COMPARE: (basePrice: string, candidatePrice: string) => `기준 ${basePrice} / 대상 ${candidatePrice}`,
  DIFF_VANISHING_NOTICE: "흡수될 옵션그룹에만 있는 옵션은 함께 감춰지며 기준 그룹으로 옮겨지지 않습니다.",

  // ===== 합치기 실행 =====

  BUTTON_MERGE: "합치기",
  DIALOG_MERGE_TITLE: "옵션그룹 합치기",
  DIALOG_MERGE_BASE: (name: string) => `기준 옵션그룹: ${name}`,
  DIALOG_MERGE_ABSORBED: (count: number) => `흡수될 옵션그룹 ${count}개`,
  DIALOG_MERGE_AFFECTED: (count: number) => `영향받는 메뉴 ${count}개`,
  /** PDF FAQ: "합친 이후에 다시 분리는 불가해요." — 비가역성을 반드시 명시한다 */
  DIALOG_MERGE_IRREVERSIBLE: "합친 뒤에는 다시 분리할 수 없어요.",
  DIALOG_MERGE_VANISHING: "흡수될 옵션그룹에만 있는 옵션은 사라집니다.",

  /**
   * 합치기 불가 사유 안내.
   *
   * 이 앱은 원칙적으로 `errorCode → 문구` 맵을 두지 않지만, `blockedReason` 은 **에러 응답이 아니라
   * 정상 응답(200)의 필드**라 서버가 한국어 문구를 함께 내려주지 않는다. 버튼을 왜 못 누르는지는
   * 화면이 설명해야 하므로 여기서만 예외적으로 코드를 문구로 옮긴다.
   */
  BLOCKED_REASON_LABEL: {
    PRODUCT_OPTION_GROUP_MERGE_SAME_PRODUCT_LINKED: "같은 메뉴에 연결된 옵션그룹끼리는 합칠 수 없어요.",
    PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY: "합칠 옵션그룹을 1개 이상 선택해주세요.",
    PRODUCT_OPTION_GROUP_MERGE_BASE_INCLUDED: "기준 옵션그룹은 합칠 대상에 포함할 수 없어요.",
    PRODUCT_OPTION_GROUP_MERGE_HIDDEN_TARGET: "삭제된 옵션그룹은 합칠 수 없어요.",
    PRODUCT_OPTION_GROUP_MERGE_TYPE_MISMATCH: "일회용컵 보증금 옵션그룹은 일반 옵션그룹과 합칠 수 없어요.",
    PRODUCT_OPTION_GROUP_SHOP_MISMATCH: "다른 가게의 옵션그룹은 합칠 수 없어요.",
    PRODUCT_OPTION_MIN_SELECT_VIOLATION: "기준 옵션그룹의 최소 선택 개수를 채울 수 없어 합칠 수 없어요.",
  } as Record<string, string>,
  BLOCKED_REASON_FALLBACK: "지금은 이 옵션그룹들을 합칠 수 없어요.",
} as const;

/** 옵션그룹 합치기 액션 결과 문구 */
export const OPTION_GROUP_MERGE_MESSAGE = {
  SUGGESTIONS_LOAD_FAILED: "합치기 추천 목록을 불러오지 못했습니다.",
  PREVIEW_LOAD_FAILED: "옵션그룹 차이를 불러오지 못했습니다.",
  EXCLUDE_SUCCESS: "추천 목록에서 제외했습니다.",
  EXCLUDE_FAILED: "추천 목록에서 제외하지 못했습니다.",
  MERGE_SUCCESS: "옵션그룹을 합쳤습니다.",
  MERGE_FAILED: "옵션그룹을 합치지 못했습니다.",
} as const;

// =====================================================================================
// 사장님 추천 (대표 메뉴) — `docs/tasks/menu-board-promotion/frontend.md` A-2
// =====================================================================================

export const PRODUCT_REPRESENTATIVE_MESSAGE = {
  LIMIT_EXCEEDED: "사장님 추천 메뉴는 최대 6개까지 등록할 수 있습니다.",
  IMAGE_REQUIRED: "이미지가 등록된 메뉴만 사장님 추천으로 설정할 수 있습니다.",
  LAST_CANNOT_RELEASE: "사장님 추천 메뉴는 최소 1개 남아 있어야 합니다.",
  REQUEST_SUCCESS: "사장님 추천 메뉴를 신청했습니다. 승인 후 반영됩니다.",
  RELEASE_SUCCESS: "사장님 추천을 해제했습니다.",
} as const;

/** 사장님 추천 시트 화면 문구 */
export const PRODUCT_REPRESENTATIVE_COPY = {
  SHEET_TITLE: "사장님 추천",
  SHEET_DESCRIPTION: "손님 화면의 메뉴에 '사장님 추천' 뱃지가 붙습니다.",
  REQUEST_FAILED: "사장님 추천 메뉴를 신청하지 못했습니다.",
  RELEASE_FAILED: "사장님 추천을 해제하지 못했습니다.",
  CURRENT_TITLE: "지정된 메뉴",
  EMPTY: "지정된 사장님 추천 메뉴가 없습니다.",
  BUTTON_ADD: "+ 사장님 추천 메뉴 추가",
  BUTTON_CLOSE: "닫기",
  ACTION_RELEASE: "해제",
  BADGE_REPRESENTATIVE: "추천",
  IMAGE_ALT_PREFIX: "메뉴 이미지 ",
  RELEASE_CONFIRM_TITLE: "사장님 추천을 해제할까요?",
  RELEASE_CONFIRM_DESCRIPTION: "해제하면 손님 화면의 뱃지가 즉시 사라집니다.",
  RELEASE_CONFIRM_ACTION: "해제",
  RELEASE_CONFIRM_CANCEL: "취소",
  PICKER_TITLE: "사장님 추천 메뉴 선택",
  PICKER_DESCRIPTION: "추천으로 지정할 메뉴를 고르세요. 지정된 메뉴와 합쳐 최대 6개까지 가능합니다.",
  PICKER_EMPTY: "선택할 수 있는 메뉴가 없습니다.",
  PICKER_SUBMIT: "적용",
  PICKER_CANCEL: "취소",
  PICKER_UNCATEGORIZED: "미분류",
  PICKER_IMAGE_MISSING_HINT: "이미지 등록 후 지정할 수 있습니다",
  PICKER_SELECTED_PREFIX: "선택 ",
  PICKER_SELECTED_SUFFIX: "개",
  CRITERIA_TITLE: "등록 기준 안내",
  CRITERIA_ITEMS: [
    "대표 메뉴는 가게 카테고리와 일치하도록 합니다(반찬·소스·음료 등 부수 메뉴는 등록 불가).",
    "대표 메뉴는 최대 6개까지 등록 가능합니다.",
    "이미지가 없는 메뉴는 대표 메뉴로 등록할 수 없습니다.",
    "대표 메뉴의 메뉴명과 메뉴 이미지가 일치해야 합니다.",
  ],
} as const;
