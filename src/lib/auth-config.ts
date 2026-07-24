/**
 * 인증 관련 쿠키 키 정의.
 *
 * admin-api 호출 시 ApiClient 가 access token 을 Authorization 헤더로 실어 보낸다.
 */

export const AUTH_COOKIE_KEYS = {
  ACCESS_TOKEN: "accessToken",
  REFRESH_TOKEN: "refreshToken",
} as const;

/**
 * 인증 실패(401) 또는 미인증 시 이동하는 로그인 경로.
 * proxy(optimistic 체크)와 ApiClient(401 감지)가 동일 경로를 참조한다.
 */
export const LOGIN_PATH = "/auth/login";
