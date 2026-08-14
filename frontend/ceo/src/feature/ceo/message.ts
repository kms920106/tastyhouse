/**
 * 점주 계정 단위 화면의 한국어 문구.
 *
 * 컴포넌트에 인라인하지 않는다(`src/feature/shop/message.ts` 의
 * `SHOP_CHANGE_HISTORY_COPY`·`SHOP_REQUEST_COPY` 선례).
 */

export const CEO_LOGIN_HISTORY_COPY = {
  TITLE: "개인정보 접속기록",
  DESCRIPTION:
    "주문접수 화면 로그인 시 회원 개인정보를 조회할 수 있어, 로그인 기록을 개인정보 접속기록으로 제공합니다.",
  RETENTION_NOTICE: "최근 90일 기록을 조회할 수 있습니다.",

  // 필터
  FILTER_RESULT_LABEL: "결과",
  FILTER_PERIOD_LABEL: "조회 기간",
  FILTER_ALL: "전체",
  SEARCH: "조회",
  RESET: "초기화",

  // 목록 항목
  IP_ADDRESS: "접속 IP",
  USER_AGENT: "접속 기기 정보",
  FAILURE_REASON: "실패 사유",
  VALUE_ABSENT: "—",

  // 빈 상태
  EMPTY_TITLE: "조회된 기록이 없습니다.",
  EMPTY_DESCRIPTION: "기록 시행 이전 기간은 조회할 수 없습니다. 기간이나 필터를 변경해 다시 조회해 주세요.",

  // 에러
  LOAD_FAILED: "기록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DATE_OUT_OF_RANGE: "조회 가능한 기간을 벗어났습니다.",
  DATE_RANGE_INVALID: "조회 시작일이 종료일보다 늦습니다.",
  INVALID_SEARCH_PARAMS: "잘못된 조회 조건입니다. 다시 시도해 주세요.",

  // 페이지네이션
  PREVIOUS_PAGE: "이전",
  NEXT_PAGE: "다음",
} as const;

export const CEO_SHOP_ACCESS_HISTORY_COPY = {
  TITLE: "시스템 접근권한 이력",
  DESCRIPTION:
    "점주 계정과 가게가 연결·해제된 기록입니다. 가게가 연결되면 해당 가게 주문의 개인정보를 조회할 수 있습니다.",
  RETENTION_NOTICE: "최근 5년 기록을 조회할 수 있습니다.",

  // 필터
  FILTER_ACTION_TYPE_LABEL: "조치 유형",
  FILTER_SHOP_LABEL: "가게",
  FILTER_PERIOD_LABEL: "조회 기간",
  FILTER_ALL: "전체",
  SEARCH: "조회",
  RESET: "초기화",
  /** 가게 Select 는 현재 배정된 가게만 담으므로 이미 해제된 가게는 고를 수 없다 */
  SHOP_FILTER_HINT: "이미 연결이 해제된 가게는 목록에 표시되지 않습니다. 전체로 조회해 주세요.",
  /** 가게 목록 조회가 실패해 가게 필터만 비활성화됐을 때 */
  SHOP_FILTER_UNAVAILABLE: "가게 목록을 불러오지 못해 가게 필터를 사용할 수 없습니다.",

  // 목록 항목
  SHOP_NAME: "가게",
  OCCURRED_AT: "조치 시각",

  // 빈 상태
  EMPTY_TITLE: "조회된 기록이 없습니다.",
  EMPTY_DESCRIPTION: "기록 시행 이전 기간은 조회할 수 없습니다. 기간이나 필터를 변경해 다시 조회해 주세요.",

  // 에러
  LOAD_FAILED: "기록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DATE_OUT_OF_RANGE: "조회 가능한 기간을 벗어났습니다.",
  DATE_RANGE_INVALID: "조회 시작일이 종료일보다 늦습니다.",
  INVALID_SEARCH_PARAMS: "잘못된 조회 조건입니다. 다시 시도해 주세요.",

  // 페이지네이션
  PREVIOUS_PAGE: "이전",
  NEXT_PAGE: "다음",
} as const;

export const CEO_ERROR_PAGE_COPY = {
  TITLE: "문제가 발생했습니다",
  RETRY: "다시 시도",
} as const;
