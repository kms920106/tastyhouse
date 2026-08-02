import 'server-only'

import { api } from '@/lib/api'
import type {
  AppleLoginRequest,
  FacebookLoginRequest,
  JwtToken,
  KakaoLoginRequest,
  LoginRequest,
  NaverLoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetVerifyRequest,
  PasswordResetVerifyResponse,
  PhoneLoginRequest,
  PhoneLoginResponse,
  SignupRequest,
  SocialAccountLinkRequest,
  SocialLinkResponse,
  SocialLoginResponse,
  SocialSignUpRequest,
} from './auth.dto'

const AUTH_BASE = '/api/auth/v1'
const AUTH_SIGNUP_ENDPOINT = '/api/auth/signup'
const PASSWORD_RESET_BASE = `${AUTH_BASE}/password-reset`

export const authRepository = {
  // 회원가입
  async signup(payload: SignupRequest) {
    return api.post<void>(AUTH_SIGNUP_ENDPOINT, payload)
  },

  // 로그인
  async login(payload: LoginRequest) {
    return api.post<JwtToken>(`${AUTH_BASE}/login`, payload)
  },

  // 로그아웃
  async logout() {
    return api.post(`${AUTH_BASE}/logout`)
  },

  // 비밀번호 찾기 - 인증코드 발송
  async requestPasswordReset(username: string) {
    return api.post<void>(`${PASSWORD_RESET_BASE}/request`, { username })
  },

  // 비밀번호 찾기 - 인증코드 확인
  async verifyPasswordReset(request: PasswordResetVerifyRequest) {
    return api.post<PasswordResetVerifyResponse>(`${PASSWORD_RESET_BASE}/verify`, request)
  },

  // 비밀번호 재설정
  async confirmPasswordReset(request: PasswordResetConfirmRequest) {
    return api.post<void>(`${PASSWORD_RESET_BASE}/confirm`, request)
  },

  // 카카오 로그인
  async kakaoLogin(request: KakaoLoginRequest) {
    return api.post<SocialLoginResponse>(`${AUTH_BASE}/login/kakao`, request)
  },

  // 네이버 로그인
  async naverLogin(request: NaverLoginRequest) {
    return api.post<SocialLoginResponse>(`${AUTH_BASE}/login/naver`, request)
  },

  // 페이스북 로그인
  async facebookLogin(request: FacebookLoginRequest) {
    return api.post<SocialLoginResponse>(`${AUTH_BASE}/login/facebook`, request)
  },

  // 애플 로그인
  async appleLogin(request: AppleLoginRequest) {
    return api.post<SocialLoginResponse>(`${AUTH_BASE}/login/apple`, request)
  },

  // 휴대폰 인증 로그인
  async phoneLogin(request: PhoneLoginRequest) {
    return api.post<PhoneLoginResponse>(`${AUTH_BASE}/login/phone`, request)
  },

  // 소셜 계정 연동
  async linkSocialAccount(request: SocialAccountLinkRequest) {
    return api.post<SocialLinkResponse>(`${AUTH_BASE}/link/social`, request)
  },

  // 소셜 회원가입
  async signUpSocial(request: SocialSignUpRequest) {
    return api.post<JwtToken>(`${AUTH_BASE}/signup/social`, request)
  },
}
