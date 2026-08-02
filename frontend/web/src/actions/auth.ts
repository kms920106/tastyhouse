'use server'

import { SocialProfile } from '@/domains/auth'
import { authRepository } from '@/domains/auth/auth.repository'
import type { SocialProvider } from '@/domains/auth/auth.types'
import type { MemberGender } from '@/domains/member'
import { AUTH_COOKIE_KEYS, getTokenMaxAge, TOKEN_MAX_AGE } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { revalidatePath } from 'next/cache'
import { cookies } from 'next/headers'
import { redirect } from 'next/navigation'

function validateLoginInput(username: string, password: string): string | null {
  if (!username?.trim()) {
    return '아이디를 입력해 주세요.'
  }
  if (!password?.trim()) {
    return '비밀번호를 입력해 주세요.'
  }
  return null
}

export type LoginResult = {
  success: false
  error: string
}

export async function loginFormAction(
  _prevState: LoginResult | null,
  formData: FormData,
): Promise<LoginResult> {
  const username = formData.get('username')?.toString() ?? ''
  const password = formData.get('password')?.toString() ?? ''
  const rememberMe = formData.get('rememberMe') === 'true'

  const validationError = validateLoginInput(username, password)
  if (validationError) {
    return { success: false, error: validationError }
  }

  const { data, error } = await authRepository.login({ username, password })

  if (error || !data) {
    return { success: false, error: '아이디 또는 비밀번호가 올바르지 않습니다.' }
  }

  const cookieStore = await cookies()
  const baseOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax' as const,
    path: '/',
  }

  const tokenMaxAge = getTokenMaxAge(rememberMe)

  const { accessToken, refreshToken } = data

  cookieStore.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, accessToken, {
    ...baseOptions,
    ...(rememberMe ? { maxAge: tokenMaxAge.accessToken } : {}),
  })
  cookieStore.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, refreshToken, {
    ...baseOptions,
    ...(rememberMe ? { maxAge: tokenMaxAge.refreshToken } : {}),
  })

  if (rememberMe) {
    cookieStore.set(AUTH_COOKIE_KEYS.REMEMBER_ME, 'true', {
      ...baseOptions,
      httpOnly: false,
      maxAge: TOKEN_MAX_AGE.REMEMBER_ME,
    })
  }

  revalidatePath('/')
  redirect('/')
}

export async function logout() {
  try {
    await authRepository.logout()

    const cookieStore = await cookies()
    cookieStore.delete(AUTH_COOKIE_KEYS.ACCESS_TOKEN)
    cookieStore.delete(AUTH_COOKIE_KEYS.REFRESH_TOKEN)

    revalidatePath('/', 'layout')
  } catch (error) {
    console.error('[Server Action] Logout error:', error)
  }

  redirect(PAGE_PATHS.HOME)
}

export async function requestPasswordReset(username: string) {
  return authRepository.requestPasswordReset(username)
}

export async function verifyPasswordReset(username: string, verificationCode: string) {
  return authRepository.verifyPasswordReset({ username, verificationCode })
}

export async function verifyPasswordResetForEmailField(
  email: string,
  verificationCode: string,
): Promise<{ error?: string; data?: { token: string } } | null> {
  const response = await verifyPasswordReset(email, verificationCode)
  if (!response || response.error) return response as { error: string } | null
  const token = response.data?.passwordResetToken
  return { data: { token: token ?? '' } }
}

export async function confirmPasswordReset(
  passwordResetToken: string,
  newPassword: string,
  newPasswordConfirm: string,
) {
  return authRepository.confirmPasswordReset({
    passwordResetToken,
    newPassword,
    newPasswordConfirm,
  })
}

async function setAuthCookies(accessToken: string, refreshToken: string) {
  const cookieStore = await cookies()
  const baseOptions = {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax' as const,
    path: '/',
  }
  cookieStore.set(AUTH_COOKIE_KEYS.ACCESS_TOKEN, accessToken, baseOptions)
  cookieStore.set(AUTH_COOKIE_KEYS.REFRESH_TOKEN, refreshToken, baseOptions)
}

export type SocialLinkAccountResult =
  | { success: true; status: 'LOGIN' }
  | {
      success: true
      status: 'NEEDS_SIGN_UP'
      tempToken: string
      socialProfile: SocialProfile | null
    }
  | { success: false; error: string }

export async function socialLinkAccountAction(
  provider: SocialProvider,
  tempToken: string,
  phoneVerifyToken: string,
): Promise<SocialLinkAccountResult> {
  const LINK_ERROR = '계정 연동에 실패했습니다. 다시 시도해 주세요.'

  const { data, error } = await authRepository.linkSocialAccount({
    provider,
    tempToken,
    phoneVerifyToken,
  })

  if (error || !data) {
    return { success: false, error: LINK_ERROR }
  }

  const { status, jwt, socialProfile } = data

  // 기존 일반 계정과 소셜 계정 연동이 완료되었으므로 JWT를 쿠키에 저장하고 로그인 처리한다.
  if (status === 'LOGIN' && jwt) {
    await setAuthCookies(jwt.accessToken, jwt.refreshToken)
    revalidatePath('/')
    return { success: true, status: 'LOGIN' }
  }

  // 해당 전화번호로 가입된 계정이 없는 신규 고객이므로 회원가입 페이지로 안내한다.
  if (status === 'NEEDS_SIGN_UP' && tempToken) {
    return { success: true, status: 'NEEDS_SIGN_UP', tempToken, socialProfile }
  }

  return { success: false, error: LINK_ERROR }
}

export type PhoneLoginResult =
  | { success: true; needsSignUp: false }
  | { success: true; needsSignUp: true }
  | { success: false; error: string }

export async function phoneLoginAction(phoneVerifyToken: string): Promise<PhoneLoginResult> {
  const { data, error } = await authRepository.phoneLogin({ phoneVerifyToken })

  if (error || !data) {
    return { success: false, error: '휴대폰 인증 로그인에 실패했습니다. 다시 시도해 주세요.' }
  }

  const { needsSignUp, jwt } = data

  if (!needsSignUp && jwt) {
    await setAuthCookies(jwt.accessToken, jwt.refreshToken)
    revalidatePath('/')
    return { success: true, needsSignUp: false }
  }

  return { success: true, needsSignUp: true }
}

export type FacebookLoginResult =
  | { success: true; status: 'LOGIN' }
  | { success: true; status: 'NEEDS_SIGN_UP' | 'NEEDS_LINKING'; tempToken: string }
  | { success: false; error: string }

export async function facebookLoginAction(accessToken: string): Promise<FacebookLoginResult> {
  const { data, error } = await authRepository.facebookLogin({ accessToken })

  if (error || !data) {
    return { success: false, error: '페이스북 로그인에 실패했습니다. 다시 시도해 주세요.' }
  }

  const { status, jwt, tempToken } = data

  if (status === 'LOGIN' && jwt) {
    await setAuthCookies(jwt.accessToken, jwt.refreshToken)
    revalidatePath('/')
    return { success: true, status: 'LOGIN' }
  }

  if ((status === 'NEEDS_SIGN_UP' || status === 'NEEDS_LINKING') && tempToken) {
    return { success: true, status, tempToken }
  }

  return { success: false, error: '페이스북 로그인에 실패했습니다. 다시 시도해 주세요.' }
}

export type SocialSignUpResult = { success: false; error: string }

export async function socialSignUpAction({
  provider,
  tempToken,
  username,
  nickname,
  fullName,
  gender,
  birthDate,
  phoneNumber,
  pushNotificationEnabled,
  marketingInfoEnabled,
  eventInfoEnabled,
  referrerNickname,
}: {
  provider: SocialProvider
  tempToken: string
  username: string
  nickname: string
  fullName: string
  gender: MemberGender
  birthDate: number
  phoneNumber: string
  pushNotificationEnabled?: boolean
  marketingInfoEnabled?: boolean
  eventInfoEnabled?: boolean
  referrerNickname?: string
}): Promise<SocialSignUpResult | null> {
  const SIGN_UP_ERROR = '회원가입에 실패했습니다. 다시 시도해 주세요.'

  const { data, error } = await authRepository.signUpSocial({
    provider,
    tempToken,
    username,
    nickname,
    fullName,
    gender,
    birthDate,
    phoneNumber,
    pushNotificationEnabled,
    marketingInfoEnabled,
    eventInfoEnabled,
    referrerNickname,
  })

  if (error || !data) {
    return { success: false, error: SIGN_UP_ERROR }
  }

  const { accessToken, refreshToken } = data

  await setAuthCookies(accessToken, refreshToken)
  revalidatePath('/')
  return null
}

export type SignupResult = {
  success: false
  error: string
}

export async function signupFormAction(
  _prevState: SignupResult | null,
  formData: FormData,
): Promise<SignupResult> {
  const email = formData.get('email')?.toString() ?? ''
  const password = formData.get('password')?.toString() ?? ''
  const fullName = formData.get('fullName')?.toString() ?? ''
  const nickname = formData.get('nickname')?.toString() ?? ''
  const phoneNumber = (formData.get('phoneNumber')?.toString() ?? '').replace(/-/g, '')
  const birthYear = formData.get('birthYear')?.toString() ?? ''
  const birthMonth = formData.get('birthMonth')?.toString().padStart(2, '0') ?? ''
  const birthDay = formData.get('birthDay')?.toString().padStart(2, '0') ?? ''
  const birthDate =
    birthYear && birthMonth && birthDay ? `${birthYear}${birthMonth}${birthDay}` : ''
  const gender = formData.get('gender')?.toString() ?? ''
  const referrerNickname = formData.get('verifiedReferrerNickname')?.toString() ?? ''
  const emailVerifyToken = formData.get('emailVerifyToken')?.toString() ?? ''
  const phoneVerifyToken = formData.get('phoneVerifyToken')?.toString() ?? ''
  const agreedMarketing = formData.has('agreedMarketing')
  const agreedPushNotification = formData.has('agreedPushNotification')

  const { error } = await authRepository.signup({
    username: email,
    password,
    fullName,
    nickname,
    phoneNumber,
    birthDate: Number(birthDate),
    gender,
    referrerNickname: referrerNickname || undefined,
    marketingInfoEnabled: agreedMarketing,
    eventInfoEnabled: agreedMarketing,
    pushNotificationEnabled: agreedPushNotification,
    emailVerifyToken,
    phoneVerifyToken,
  })

  if (error) {
    return { success: false, error: '회원가입에 실패했습니다. 다시 시도해 주세요.' }
  }

  redirect('/auth/signup/complete')
}
