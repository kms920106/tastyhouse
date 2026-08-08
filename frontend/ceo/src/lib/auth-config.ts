/**
 * 인증 관련 쿠키 키 정의.
 *
 * ceo-api 호출 시 ApiClient 가 access token 을 Authorization 헤더로 실어 보낸다.
 *
 * 쿠키 이름에 `th_ceo_` 접두사를 붙여 web/admin 앱과 이름 공간을 분리한다. 브라우저 쿠키
 * 저장소는 포트를 구분하지 않으므로, 로컬 개발에서 세 앱이 모두 localhost 를 쓰면 같은
 * 이름의 토큰 쿠키가 서로 덮어써져 앱을 전환할 때마다 로그아웃되는 문제가 있었다.
 * (운영에서 서브도메인을 쓰더라도 부모 도메인 쿠키가 섞이는 것을 함께 막아 준다.)
 */

export const AUTH_COOKIE_KEYS = {
  ACCESS_TOKEN: "th_ceo_accessToken",
  REFRESH_TOKEN: "th_ceo_refreshToken",
  /**
   * 로그인 시 선택한 "로그인 상태 유지" 여부.
   *
   * 토큰 갱신(refresh)은 요청 본문에 rememberMe 를 받지 않으므로, 갱신된 토큰을 쿠키에
   * 다시 심을 때 원래 세션 길이(30일/7일)를 알 방법이 없다. 이 값을 별도 쿠키로 남겨
   * 갱신 후에도 사용자가 선택한 만료기간이 유지되도록 한다.
   */
  REMEMBER_ME: "th_ceo_rememberMe",
} as const;

/**
 * 인증 쿠키 만료기간(초).
 *
 * 명세상 refreshToken 만료는 rememberMe 에 따라 30일/7일이며, 프론트 쿠키도 동일한
 * 기간을 따라가 "쿠키는 살아있는데 서버 토큰은 만료" 상태를 최소화한다.
 */
export const AUTH_SESSION_MAX_AGE = {
  REMEMBERED: 60 * 60 * 24 * 30,
  DEFAULT: 60 * 60 * 24 * 7,
} as const;

/**
 * 인증 실패(401) 또는 미인증 시 이동하는 로그인 경로.
 * proxy(optimistic 체크)와 ApiClient(401 감지)가 동일 경로를 참조한다.
 */
export const LOGIN_PATH = "/auth/login";
