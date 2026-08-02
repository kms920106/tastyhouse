/** 랭킹 화면 헤더 정적 문구 (목록/로딩 공용) */
export const RANK_PAGE_COPY = {
  TITLE: "랭킹",
  DESCRIPTION: "회원 랭킹을 조회하고 랭킹 기간·경품을 관리합니다.",
} as const;

/** 랭킹 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const RANK_MESSAGE = {
  // 성공 toast
  AGGREGATION_SUCCESS: "랭킹 집계를 실행했습니다.",
  PERIOD_CREATE_SUCCESS: "랭킹 기간이 등록되었습니다.",
  PERIOD_UPDATE_SUCCESS: "랭킹 기간이 수정되었습니다.",
  PERIOD_DELETE_SUCCESS: "랭킹 기간이 삭제되었습니다.",
  PRIZE_CREATE_SUCCESS: "경품이 등록되었습니다.",
  PRIZE_UPDATE_SUCCESS: "경품이 수정되었습니다.",
  PRIZE_DELETE_SUCCESS: "경품이 삭제되었습니다.",

  // 에러 폴백
  AGGREGATION_FAILED: "랭킹 집계 실행 중 오류가 발생했습니다.",
  PERIOD_CREATE_UPDATE_FAILED: "랭킹 기간 처리 중 오류가 발생했습니다.",
  PERIOD_DELETE_FAILED: "랭킹 기간 삭제 중 오류가 발생했습니다.",
  PRIZE_CREATE_UPDATE_FAILED: "경품 처리 중 오류가 발생했습니다.",
  PRIZE_DELETE_FAILED: "경품 삭제 중 오류가 발생했습니다.",
  PRIZE_RANK_DUPLICATED: "이미 같은 등수의 경품이 있습니다.",
  IMAGE_UPLOAD_FAILED: "이미지 업로드 중 오류가 발생했습니다.",
  IMAGE_TYPE_INVALID: "jpg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.",
  IMAGE_SIZE_EXCEEDED: "이미지 크기는 최대 10MB까지 업로드할 수 있습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  MEMBERS_LOAD_FAILED: "회원 랭킹을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  PERIODS_LOAD_FAILED: "랭킹 기간 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  PERIOD_DETAIL_LOAD_FAILED: "랭킹 기간 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  PRIZES_LOAD_FAILED: "경품 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
