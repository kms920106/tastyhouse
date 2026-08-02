import { env } from '@/lib/env'

export function getKakaoAuthUrl(): string {
  const url = new URL('https://kauth.kakao.com/oauth/authorize')
  url.searchParams.set('client_id', process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY ?? '')
  url.searchParams.set('redirect_uri', `${env.NEXT_PUBLIC_SITE_URL}/auth/callback/kakao`)
  url.searchParams.set('response_type', 'code')
  url.searchParams.set('scope', 'profile_nickname profile_image account_email')
  return url.toString()
}

export function getNaverAuthUrl(): string {
  const url = new URL('https://nid.naver.com/oauth2.0/authorize')
  url.searchParams.set('response_type', 'code')
  url.searchParams.set('client_id', env.NEXT_PUBLIC_NAVER_CLIENT_ID)
  url.searchParams.set('redirect_uri', `${env.NEXT_PUBLIC_SITE_URL}/auth/callback/naver`)
  url.searchParams.set('state', crypto.randomUUID())
  return url.toString()
}

export function getAppleAuthUrl(): string {
  const url = new URL('https://appleid.apple.com/auth/authorize')
  url.searchParams.set('client_id', env.NEXT_PUBLIC_APPLE_CLIENT_ID)
  url.searchParams.set('redirect_uri', `${env.NEXT_PUBLIC_SITE_URL}/auth/callback/apple`)
  url.searchParams.set('response_type', 'code id_token')
  url.searchParams.set('scope', 'name email')
  url.searchParams.set('response_mode', 'form_post')
  url.searchParams.set('state', crypto.randomUUID())
  return url.toString()
}
