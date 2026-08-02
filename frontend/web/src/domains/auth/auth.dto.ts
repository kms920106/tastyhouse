import type { MemberGender } from '@/domains/member'
import type { SocialLoginStatus, SocialLinkStatus, SocialProvider } from './auth.types'
import type { SocialProfile } from './auth.model'

export interface JwtToken {
  accessToken: string
  refreshToken: string
  tokenType: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface PasswordResetRequest {
  username: string
}

export interface PasswordResetVerifyRequest {
  username: string
  verificationCode: string
}

export interface PasswordResetVerifyResponse {
  passwordResetToken: string
}

export interface PasswordResetConfirmRequest {
  passwordResetToken: string
  newPassword: string
  newPasswordConfirm: string
}

export interface SocialLoginResponse {
  status: SocialLoginStatus
  tempToken: string | null
  jwt: JwtToken | null
}

export interface SocialLinkResponse {
  status: SocialLinkStatus
  tempToken: string | null
  jwt: JwtToken | null
  socialProfile: SocialProfile | null
}

export interface PhoneLoginRequest {
  phoneVerifyToken: string
}

export interface PhoneLoginResponse {
  needsSignUp: boolean
  jwt: JwtToken | null
}

export interface SocialAccountLinkRequest {
  provider: SocialProvider
  tempToken: string
  phoneVerifyToken: string
}

export interface KakaoLoginRequest {
  code: string
}

export interface NaverLoginRequest {
  code: string
  state: string
}

export interface FacebookLoginRequest {
  accessToken: string
}

export interface AppleLoginRequest {
  code: string
  idToken: string
}

export interface SignupRequest {
  username: string
  password: string
  fullName: string
  nickname: string
  phoneNumber: string
  birthDate: number
  gender: string
  referrerNickname?: string
  marketingInfoEnabled: boolean
  eventInfoEnabled: boolean
  pushNotificationEnabled: boolean
  emailVerifyToken: string
  phoneVerifyToken: string
}

export interface SocialSignUpRequest {
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
}
