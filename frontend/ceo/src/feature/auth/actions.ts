"use server";

import { redirect } from "next/navigation";

import { authRepository } from "@/api/auth/auth.repository";
import { AUTH_COOKIE_KEYS, AUTH_SESSION_MAX_AGE, LOGIN_PATH } from "@/lib/auth-config";
import { getValueFromCookie, removeCookie, setValueToCookie } from "@/server/server-actions";

import { AUTH_MESSAGE } from "./message";
import { type LoginFormValues, loginFormSchema } from "./schema";

/** 로그인 Rate Limit 초과 상태코드 (IP당 60초 10회) */
const HTTP_TOO_MANY_REQUESTS = 429;

export interface AuthActionResult {
  success: boolean;
  message?: string;
}

/** accessToken·refreshToken·rememberMe 쿠키를 동일한 만료기간으로 저장한다. */
async function persistSession(
  tokens: { accessToken: string; refreshToken: string },
  rememberMe: boolean,
): Promise<void> {
  const maxAge = rememberMe ? AUTH_SESSION_MAX_AGE.REMEMBERED : AUTH_SESSION_MAX_AGE.DEFAULT;
  // JWT 는 XSS 로 탈취되면 안 되므로 httpOnly 로 심는다. rememberMe 도 서버(proxy/actions)만
  // 읽으므로 동일하게 감춘다.
  const cookieOptions = { maxAge, httpOnly: true } as const;

  await Promise.all([
    setValueToCookie(AUTH_COOKIE_KEYS.ACCESS_TOKEN, tokens.accessToken, cookieOptions),
    setValueToCookie(AUTH_COOKIE_KEYS.REFRESH_TOKEN, tokens.refreshToken, cookieOptions),
    setValueToCookie(AUTH_COOKIE_KEYS.REMEMBER_ME, String(rememberMe), cookieOptions),
  ]);
}

/** 인증 쿠키를 모두 제거한다 (로그아웃·갱신 실패 공통). */
async function clearSession(): Promise<void> {
  await Promise.all(Object.values(AUTH_COOKIE_KEYS).map((key) => removeCookie(key)));
}

// 로그인
export async function loginAction(values: LoginFormValues): Promise<AuthActionResult> {
  const parsed = loginFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? AUTH_MESSAGE.INVALID_INPUT,
    };
  }

  const { username, password } = parsed.data;
  const rememberMe = parsed.data.rememberMe ?? false;
  const res = await authRepository.login({ username, password, rememberMe });

  // Rate Limit(429)은 인증 실패가 아니므로 "아이디/비밀번호가 틀렸다"고 오인시키지 않는다.
  // 백엔드 메시지는 비어 있거나(명세상 message: null) 영문일 수 있어 자체 문구를 사용한다.
  if (res.status === HTTP_TOO_MANY_REQUESTS) {
    return { success: false, message: AUTH_MESSAGE.LOGIN_RATE_LIMITED };
  }

  if (res.error !== undefined) {
    return { success: false, message: res.error };
  }

  if (!res.data) {
    return { success: false, message: AUTH_MESSAGE.LOGIN_FAILED };
  }

  await persistSession(res.data, rememberMe);

  return { success: true };
}

// 토큰 갱신
export async function refreshAction(): Promise<AuthActionResult> {
  const refreshToken = await getValueFromCookie(AUTH_COOKIE_KEYS.REFRESH_TOKEN);

  if (!refreshToken) {
    return { success: false, message: AUTH_MESSAGE.REFRESH_FAILED };
  }

  const res = await authRepository.refresh({ refreshToken });

  // 갱신 실패(만료·블랙리스트 등록된 refreshToken)면 쓸 수 없는 자격증명이므로 정리한다.
  // 남겨두면 매 요청마다 갱신을 재시도하며 "로그인된 듯 보이지만 아무것도 안 되는" 상태가 된다.
  if (res.error !== undefined || !res.data) {
    await clearSession();
    return { success: false, message: res.error ?? AUTH_MESSAGE.REFRESH_FAILED };
  }

  // 갱신 응답에는 rememberMe 가 없으므로 로그인 시 저장한 쿠키에서 원래 세션 길이를
  // 되살린다. 이 값이 없으면 30일 세션이 갱신 한 번에 7일로 줄어든다.
  const rememberMe = (await getValueFromCookie(AUTH_COOKIE_KEYS.REMEMBER_ME)) === "true";

  await persistSession(res.data, rememberMe);

  return { success: true };
}

// 로그아웃
export async function logoutAction(): Promise<void> {
  const accessToken = await getValueFromCookie(AUTH_COOKIE_KEYS.ACCESS_TOKEN);

  // 백엔드 로그아웃(블랙리스트 등록·Refresh Token 삭제)을 시도하되, 결과와 무관하게
  // 클라이언트 자격증명은 반드시 제거한다. 서버 오류·네트워크 장애·토큰 만료(401) 어느
  // 경우든 "로그아웃했는데 여전히 인증된" 상태를 남기지 않는 것이 보안상 안전하다.
  if (accessToken) {
    await authRepository.logout(accessToken).catch(() => null);
  }

  await clearSession();

  redirect(LOGIN_PATH);
}
