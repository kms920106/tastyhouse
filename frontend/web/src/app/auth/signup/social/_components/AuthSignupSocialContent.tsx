'use client'

import { socialSignUpAction } from '@/actions/auth'
import type { SocialProfile, SocialProvider } from '@/domains/auth'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import AuthSignupSocialPhoneVerificationStep from './AuthSignupSocialPhoneVerificationStep'
import AuthSignupSocialForm from './AuthSignupSocialForm'

const PROVIDER_MAP: Record<string, SocialProvider> = {
  kakao: 'KAKAO',
  naver: 'NAVER',
  facebook: 'FACEBOOK',
  apple: 'APPLE',
}

interface SignupStepData {
  provider: SocialProvider
  tempToken: string
  socialProfile: SocialProfile | null
  phone: string
  phoneVerifyToken: string
}

interface Props {
  providerParam: string | undefined
  tempTokenParam: string | undefined
  onStepChange: (step: 'phone' | 'signup') => void
}

export default function AuthSignupSocialContent({
  providerParam,
  tempTokenParam,
  onStepChange,
}: Props) {
  const router = useRouter()
  const [signupData, setSignupData] = useState<SignupStepData | null>(null)

  const provider: SocialProvider | null = providerParam
    ? (PROVIDER_MAP[providerParam] ?? null)
    : null
  const tempToken = tempTokenParam ?? ''

  if (!provider) {
    router.replace(PAGE_PATHS.AUTH_LOGIN)
    return null
  }

  return (
    <>
      {signupData ? (
        <AuthSignupSocialForm
          socialProfile={signupData.socialProfile}
          phone={signupData.phone}
          onSignUp={(formData) =>
            socialSignUpAction({
              provider: signupData.provider,
              tempToken: signupData.tempToken,
              ...formData,
            })
          }
        />
      ) : (
        <AuthSignupSocialPhoneVerificationStep
          provider={provider}
          tempToken={tempToken}
          onLinked={() => router.replace(PAGE_PATHS.HOME)}
          onNeedsSignUp={(params) => {
            setSignupData({ provider, ...params })
            onStepChange('signup')
          }}
        />
      )}
    </>
  )
}
