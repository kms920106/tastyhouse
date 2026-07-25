import { type NextRequest, NextResponse } from "next/server";

import { authRepository } from "@/api/auth/auth.repository";
import { AUTH_COOKIE_KEYS, AUTH_SESSION_MAX_AGE, LOGIN_PATH } from "@/lib/auth-config";

/** 미인증 상태로 판단해 로그인 페이지로 보낸다. 원래 경로는 callbackUrl 로 보존한다. */
function redirectToLogin(req: NextRequest, pathname: string) {
  const url = req.nextUrl.clone();
  url.pathname = LOGIN_PATH;
  url.searchParams.set("callbackUrl", pathname);

  const res = NextResponse.redirect(url);
  // 만료·무효 토큰이 남아 다음 요청에서 또 갱신을 시도하지 않도록 정리한다.
  for (const key of Object.values(AUTH_COOKIE_KEYS)) {
    res.cookies.delete(key);
  }
  return res;
}

/**
 * 요청 완료 전 실행되는 Proxy (Next.js 16, 구 middleware).
 *
 * accessToken 쿠키가 있으면 통과시키고, 없고 refreshToken 만 남아 있으면(= access 쿠키
 * 만료) 토큰 갱신을 시도해 세션을 이어준다. Server Component 에서는 쿠키를 쓸 수 없어
 * ApiClient 가 갱신을 수행할 수 없으므로, 응답에 쿠키를 실을 수 있는 이 지점에서 처리한다.
 * 토큰의 위조 여부는 검증하지 않으며(optimistic), 그 판별은 백엔드 401 을 받는 ApiClient 가 맡는다.
 */
export async function proxy(req: NextRequest) {
  const { pathname } = req.nextUrl;

  // 인증 페이지(로그인/회원가입 등)는 통과시켜 리다이렉트 루프를 방지한다.
  if (pathname.startsWith("/auth")) {
    return NextResponse.next();
  }

  const accessToken = req.cookies.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)?.value;

  if (accessToken) {
    return NextResponse.next();
  }

  // accessToken 쿠키만 만료된 경우: refreshToken 으로 재발급을 시도한다.
  const refreshToken = req.cookies.get(AUTH_COOKIE_KEYS.REFRESH_TOKEN)?.value;

  if (!refreshToken) {
    return redirectToLogin(req, pathname);
  }

  const refreshed = await authRepository.refresh({ refreshToken });

  if (!refreshed.data) {
    // 만료·블랙리스트 등록된 refreshToken — 재로그인이 필요하다.
    return redirectToLogin(req, pathname);
  }

  // 로그인 시 선택한 세션 길이를 유지한다 (갱신 응답에는 rememberMe 가 없다).
  const rememberMe = req.cookies.get(AUTH_COOKIE_KEYS.REMEMBER_ME)?.value === "true";
  const maxAge = rememberMe ? AUTH_SESSION_MAX_AGE.REMEMBERED : AUTH_SESSION_MAX_AGE.DEFAULT;

  const res = NextResponse.next();
  res.cookies.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, refreshed.data.accessToken, { path: "/", maxAge });
  res.cookies.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, refreshed.data.refreshToken, { path: "/", maxAge });
  res.cookies.set(AUTH_COOKIE_KEYS.REMEMBER_ME, String(rememberMe), { path: "/", maxAge });
  return res;
}

/**
 * 정적 자산·내부 라우트를 제외한 모든 경로에서 실행한다.
 * (_next/static, _next/image, favicon.ico, 확장자 포함 정적 파일 제외)
 */
export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|.*\\.).*)"],
};
