import { authRepository } from '@/domains/auth/auth.repository'
import { AUTH_COOKIE_KEYS } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { revalidatePath } from 'next/cache'
import { cookies } from 'next/headers'
import { type NextRequest, NextResponse } from 'next/server'

// Apple은 response_mode=form_post 방식으로 POST 요청을 보낸다
export async function POST(request: NextRequest) {
  const loginFailedUrl = new URL(`${PAGE_PATHS.AUTH_LOGIN}?error=apple_login_failed`, request.url)
  const authFailedUrl = new URL(`${PAGE_PATHS.AUTH_LOGIN}?error=apple_auth_failed`, request.url)

  let code: string | null = null
  let idToken: string | null = null
  let error: string | null = null

  try {
    const formData = await request.formData()
    code = formData.get('code') as string | null
    idToken = formData.get('id_token') as string | null
    error = formData.get('error') as string | null
  } catch {
    return NextResponse.redirect(authFailedUrl)
  }

  if (error || !code || !idToken) {
    return NextResponse.redirect(authFailedUrl)
  }

  const { data, error: apiError } = await authRepository.appleLogin({ code, idToken })

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
    signupUrl.searchParams.set('provider', 'apple')
    signupUrl.searchParams.set('tempToken', tempToken)
    return NextResponse.redirect(signupUrl)
  }

  return NextResponse.redirect(loginFailedUrl)
}
