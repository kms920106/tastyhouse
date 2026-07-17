/** 회원 화면 헤더 정적 문구 (목록/로딩 공용) */
export const MEMBER_PAGE_COPY = {
  TITLE: "회원",
  DESCRIPTION: "회원 목록을 조회하고 정지, 정지 해제, 강제 탈퇴를 관리합니다.",
} as const;

/** 회원 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const MEMBER_MESSAGE = {
  // 성공 toast
  SUSPEND_SUCCESS: "회원을 정지했습니다.",
  ACTIVATE_SUCCESS: "회원 정지를 해제했습니다.",
  WITHDRAWAL_SUCCESS: "회원을 강제 탈퇴 처리했습니다.",

  // 에러 폴백
  SUSPEND_FAILED: "회원 정지 처리 중 오류가 발생했습니다.",
  ACTIVATE_FAILED: "회원 정지 해제 중 오류가 발생했습니다.",
  WITHDRAWAL_FAILED: "회원 강제 탈퇴 처리 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "회원 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "회원 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
