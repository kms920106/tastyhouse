import { type NextRequest, NextResponse } from "next/server";

import { AUTH_COOKIE_KEYS, LOGIN_PATH } from "@/lib/auth-config";

/**
 * 요청 완료 전 실행되는 Proxy (Next.js 16, 구 middleware).
 *
 * Optimistic 인증 체크: accessToken 쿠키의 "존재 여부"만 가볍게 검사한다.
 * 토큰의 실제 유효성(만료/위조)은 검증하지 않으며, 그 판별은 백엔드 401 응답을
 * 받는 ApiClient(Data Access Layer)에서 처리한다.
 */
export function proxy(req: NextRequest) {
  const { pathname } = req.nextUrl;

  // 인증 페이지(로그인/회원가입 등)는 통과시켜 리다이렉트 루프를 방지한다.
  if (pathname.startsWith("/auth")) {
    return NextResponse.next();
  }

  const token = req.cookies.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)?.value;

  if (!token) {
    const url = req.nextUrl.clone();
    url.pathname = LOGIN_PATH;
    // 로그인 후 원래 페이지로 복귀할 수 있도록 현재 경로를 보존한다.
    url.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

/**
 * 정적 자산·내부 라우트를 제외한 모든 경로에서 실행한다.
 * (_next/static, _next/image, favicon.ico, 확장자 포함 정적 파일 제외)
 */
export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|.*\\.).*)"],
};
