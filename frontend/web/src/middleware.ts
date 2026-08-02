import { AUTH_COOKIE_KEYS, getTokenMaxAge } from '@/lib/auth-config'
import { getEpochMs } from '@/lib/date'
import { env } from '@/lib/env'
import { NextRequest, NextResponse } from 'next/server'

/**
 * CSRF Origin 검증
 * - 상태 변이 요청(POST, PUT, DELETE, PATCH)에 대해 Origin/Referer 헤더 검증
 * - NEXT_PUBLIC_SITE_URL 환경 변수를 허용 Origin으로 사용
 * - Origin, Referer 모두 없는 경우 서버→서버 요청으로 간주하여 통과
 */
function validateCsrfOrigin(request: NextRequest): boolean {
  const method = request.method.toUpperCase()

  if (['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    return true
  }

  const allowedOrigin = env.NEXT_PUBLIC_SITE_URL

  const origin = request.headers.get('origin')
  const referer = request.headers.get('referer')

  if (origin) {
    return origin === allowedOrigin
  }

  if (referer) {
    return referer.startsWith(allowedOrigin)
  }

  return true
}

/**
 * Middleware for automatic token refresh
 * - 모든 요청 전에 accessToken 만료 여부 확인
 * - 만료 시 자동으로 refreshToken으로 갱신
 * - 브라우저 쿠키에 새 토큰 자동 저장
 */
export async function middleware(request: NextRequest) {
  if (!validateCsrfOrigin(request)) {
    return new NextResponse('Forbidden', { status: 403 })
  }

  const accessToken = request.cookies.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)?.value
  const refreshToken = request.cookies.get(AUTH_COOKIE_KEYS.REFRESH_TOKEN)?.value
  const rememberMe = request.cookies.get(AUTH_COOKIE_KEYS.REMEMBER_ME)?.value === 'true'

  // refreshToken이 없으면 로그인 필요 (갱신 불가)
  if (!refreshToken) {
    return NextResponse.next()
  }

  // accessToken이 있으면 만료 여부 확인
  if (accessToken) {
    const isExpired = isTokenExpired(accessToken)

    // 토큰이 유효하면 그대로 진행
    if (!isExpired) {
      return NextResponse.next()
    }
  }

  // 토큰 갱신 시도
  try {
    const baseURL = env.NEXT_PUBLIC_API_URL

    const response = await fetch(`${baseURL}/api/auth/v1/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })

    if (!response.ok) {
      const res = NextResponse.next()
      res.cookies.delete(AUTH_COOKIE_KEYS.ACCESS_TOKEN)
      res.cookies.delete(AUTH_COOKIE_KEYS.REFRESH_TOKEN)
      return res
    }

    const json = await response.json()
    const data = json?.data ?? json

    // 새 토큰으로 쿠키 업데이트하여 다음 요청으로 진행
    // rememberMe 사용자는 로그인 시 설정한 maxAge(30일)를 유지
    const { accessToken: accessTokenMaxAge, refreshToken: refreshTokenMaxAge } =
      getTokenMaxAge(rememberMe)

    const res = NextResponse.next()
    res.cookies.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, data.accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax', // 외부 리다이렉트(토스페이먼츠) 허용
      path: '/',
      maxAge: accessTokenMaxAge,
    })
    res.cookies.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, data.refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax', // 외부 리다이렉트(토스페이먼츠) 허용
      path: '/',
      maxAge: refreshTokenMaxAge,
    })

    return res
  } catch (error) {
    console.error('[middleware] Token refresh error:', error)

    // 갱신 실패 시 만료된 토큰 제거 — 각 페이지에서 비인증 상태로 적절히 처리
    const res = NextResponse.next()
    res.cookies.delete(AUTH_COOKIE_KEYS.ACCESS_TOKEN)
    res.cookies.delete(AUTH_COOKIE_KEYS.REFRESH_TOKEN)
    return res
  }
}

/**
 * JWT 토큰 만료 여부 확인
 * - Base64 디코딩하여 exp (만료 시간) 확인
 * - 만료 5분 전이면 갱신 (여유 시간 확보)
 */
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000 // seconds to milliseconds
    const now = getEpochMs()
    const bufferTime = 5 * 60 * 1000 // 5분 여유

    return now >= exp - bufferTime
  } catch {
    return true // 파싱 실패 시 만료로 간주
  }
}

/**
 * Middleware 실행 경로 설정
 * - /api/auth/refresh는 제외 (무한 루프 방지)
 * - 정적 파일, Next.js 내부 파일 제외
 */
export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * 1. /api/auth/refresh (토큰 갱신 API)
     * 2. /_next (Next.js internals)
     * 3. /_static (static files)
     * 4. /_vercel (Vercel internals)
     * 5. /favicon.ico, /sitemap.xml, /robots.txt (static files)
     */
    '/((?!api/auth/refresh|_next|_static|_vercel|favicon.ico|sitemap.xml|robots.txt).*)',
  ],
}
