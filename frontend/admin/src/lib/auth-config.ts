/**
 * 인증 관련 쿠키 키 정의.
 *
 * admin-api 호출 시 ApiClient 가 access token 을 Authorization 헤더로 실어 보낸다.
 *
 * 쿠키 이름에 `th_admin_` 접두사를 붙여 web/ceo 앱과 이름 공간을 분리한다. 브라우저 쿠키
 * 저장소는 포트를 구분하지 않으므로, 로컬 개발에서 세 앱이 모두 localhost 를 쓰면 같은
 * 이름의 토큰 쿠키가 서로 덮어써져 앱을 전환할 때마다 로그아웃되는 문제가 있었다.
 * (운영에서 서브도메인을 쓰더라도 부모 도메인 쿠키가 섞이는 것을 함께 막아 준다.)
 */

export const AUTH_COOKIE_KEYS = {
  ACCESS_TOKEN: "th_admin_accessToken",
  REFRESH_TOKEN: "th_admin_refreshToken",
} as const;

/**
 * 인증 실패(401) 또는 미인증 시 이동하는 로그인 경로.
 * proxy(optimistic 체크)와 ApiClient(401 감지)가 동일 경로를 참조한다.
 */
export const LOGIN_PATH = "/auth/login";
