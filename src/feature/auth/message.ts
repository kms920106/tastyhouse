/** 인증 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const AUTH_MESSAGE = {
  // 에러 폴백
  LOGIN_FAILED: "아이디 또는 비밀번호가 올바르지 않습니다.",
  LOGOUT_FAILED: "로그아웃 중 오류가 발생했습니다.",
  REFRESH_FAILED: "토큰 갱신에 실패했습니다. 다시 로그인해 주세요.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
} as const;
