import { authRepository } from '@/domains/auth/auth.repository'
import { AUTH_COOKIE_KEYS } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { revalidatePath } from 'next/cache'
import { cookies } from 'next/headers'
import { type NextRequest, NextResponse } from 'next/server'

export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl
  const code = searchParams.get('code')
  const state = searchParams.get('state')
  const error = searchParams.get('error')

  const loginFailedUrl = new URL(`${PAGE_PATHS.AUTH_LOGIN}?error=naver_login_failed`, request.url)
  const authFailedUrl = new URL(`${PAGE_PATHS.AUTH_LOGIN}?error=naver_auth_failed`, request.url)

  if (error || !code || !state) {
    return NextResponse.redirect(authFailedUrl)
  }

  const { data, error: apiError } = await authRepository.naverLogin({ code, state })

  if (apiError || !data) {
    return NextResponse.redirect(loginFailedUrl)
  }

  const { status, jwt, tempToken } = data

  if (status === 'LOGIN' && jwt) {
    const cookieStore = await cookies()
    const baseOptions = {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax' as const,
      path: '/',
    }
    cookieStore.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, jwt.accessToken, baseOptions)
    cookieStore.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, jwt.refreshToken, baseOptions)
    revalidatePath('/')

    return NextResponse.redirect(new URL(PAGE_PATHS.HOME, request.url))
  }

  if ((status === 'NEEDS_SIGN_UP' || status === 'NEEDS_LINKING') && tempToken) {
    const signupUrl = new URL(PAGE_PATHS.AUTH_SIGNUP_SOCIAL, request.url)
    signupUrl.searchParams.set('provider', 'naver')
    signupUrl.searchParams.set('tempToken', tempToken)
    return NextResponse.redirect(signupUrl)
  }

  return NextResponse.redirect(loginFailedUrl)
}
