'use client'

import { facebookLoginAction } from '@/actions/auth'
import AppFullButton from '@/components/ui/AppFullButton'
import { toast } from '@/components/ui/AppToaster'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { FaApple, FaFacebookF } from 'react-icons/fa'
import { RiKakaoTalkFill } from 'react-icons/ri'
import { SiNaver } from 'react-icons/si'
import { getAppleAuthUrl, getKakaoAuthUrl, getNaverAuthUrl } from '../_lib/socialLoginHandlers'

export default function LoginSocialButtons() {
  const router = useRouter()

  const handleKakaoLogin = () => {
    window.location.href = getKakaoAuthUrl()
  }

  const handleNaverLogin = () => {
    window.location.href = getNaverAuthUrl()
  }

  const handleAppleLogin = () => {
    window.location.href = getAppleAuthUrl()
  }

  const handleFacebookLogin = () => {
    if (typeof window === 'undefined' || !window.FB) {
      toast('페이스북 SDK가 로드되지 않았습니다. 잠시 후 다시 시도해 주세요.')
      return
    }

    window.FB.login(
      async (response) => {
        if (response.status !== 'connected' || !response.authResponse?.accessToken) {
          toast('페이스북 로그인이 취소되었습니다.')
          return
        }

        const result = await facebookLoginAction(response.authResponse.accessToken)

        if (!result.success) {
          toast(result.error)
          return
        }

        if (result.status === 'LOGIN') {
          window.location.href = PAGE_PATHS.HOME
          return
        }

        if (result.status === 'NEEDS_SIGN_UP' || result.status === 'NEEDS_LINKING') {
          router.push(
            `${PAGE_PATHS.AUTH_SIGNUP_SOCIAL}?provider=facebook&tempToken=${result.tempToken}`,
          )
        }
      },
      { scope: 'public_profile,email' },
    )
  }

  return (
    <div className="mt-[60px] space-y-2.5">
      <AppFullButton
        type="button"
        onClick={handleKakaoLogin}
        className="relative bg-[#FEE500] text-black hover:bg-[#FEE500]/90 hover:text-black"
      >
        <span className="absolute left-4">
          <RiKakaoTalkFill className="size-6" />
        </span>
        <span className="flex-1 text-center">카카오톡으로 로그인</span>
      </AppFullButton>

      <AppFullButton
        type="button"
        onClick={handleNaverLogin}
        className="relative bg-[#03A94D] text-white hover:bg-[#03A94D]/90"
      >
        <span className="absolute left-5">
          <SiNaver className="size-4" />
        </span>
        <span className="flex-1 text-center">네이버로 로그인</span>
      </AppFullButton>

      <AppFullButton
        type="button"
        onClick={handleFacebookLogin}
        className="relative bg-[#4267b2] text-white hover:bg-[#4267b2]/90"
      >
        <span className="absolute left-4">
          <FaFacebookF className="size-6" />
        </span>
        <span className="flex-1 text-center">페이스북으로 로그인</span>
      </AppFullButton>

      <AppFullButton
        type="button"
        onClick={handleAppleLogin}
        className="relative bg-black text-white hover:bg-black/90"
      >
        <span className="absolute left-4">
          <FaApple className="size-6" />
        </span>
        <span className="flex-1 text-center">Apple로 로그인</span>
      </AppFullButton>
    </div>
  )
}
