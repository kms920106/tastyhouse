import { cookies } from 'next/headers'

/**
 * 인증 관련 쿠키 키 정의.
 *
 * 쿠키 이름에 `th_web_` 접두사를 붙여 admin/ceo 앱과 이름 공간을 분리한다. 브라우저 쿠키
 * 저장소는 포트를 구분하지 않으므로, 로컬 개발에서 세 앱이 모두 localhost 를 쓰면 같은
 * 이름의 토큰 쿠키가 서로 덮어써져 앱을 전환할 때마다 로그아웃되는 문제가 있었다.
 * (운영에서 서브도메인을 쓰더라도 부모 도메인 쿠키가 섞이는 것을 함께 막아 준다.)
 */
export const AUTH_COOKIE_KEYS = {
  ACCESS_TOKEN: 'th_web_accessToken',
  REFRESH_TOKEN: 'th_web_refreshToken',
  REMEMBER_ME: 'th_web_rememberMe',
} as const

export async function getIsLoggedIn(): Promise<boolean> {
  const cookieStore = await cookies()
  return !!cookieStore.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)
}

export const TOKEN_MAX_AGE = {
  ACCESS_TOKEN: 60 * 60, // 1시간
  REFRESH_TOKEN: 60 * 60 * 24 * 7, // 7일
  REMEMBER_ME: 60 * 60 * 24 * 30, // 30일
} as const

export function getTokenMaxAge(rememberMe: boolean) {
  return {
    accessToken: rememberMe ? TOKEN_MAX_AGE.REMEMBER_ME : TOKEN_MAX_AGE.ACCESS_TOKEN,
    refreshToken: rememberMe ? TOKEN_MAX_AGE.REMEMBER_ME : TOKEN_MAX_AGE.REFRESH_TOKEN,
  }
}
