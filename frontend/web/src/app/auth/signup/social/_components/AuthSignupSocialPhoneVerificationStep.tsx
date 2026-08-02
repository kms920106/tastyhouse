'use client'

import { socialLinkAccountAction } from '@/actions/auth'
import AppFormField from '@/components/ui/AppFormField'
import AppInputPhone from '@/components/ui/AppInputPhone'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import { toast } from '@/components/ui/AppToaster'
import { PHONE_ERROR_MESSAGES, PHONE_REGEX } from '@/constants/validation'
import type { SocialProfile, SocialProvider } from '@/domains/auth'
import { usePhoneAuth } from '@/hooks/usePhoneAuth'
import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import { useRouter } from 'next/navigation'
import { useState, useTransition } from 'react'

interface Props {
  provider: SocialProvider
  tempToken: string
  onLinked: (params: { phone: string; phoneVerifyToken: string }) => void
  onNeedsSignUp: (params: {
    tempToken: string
    socialProfile: SocialProfile | null
    phone: string
    phoneVerifyToken: string
  }) => void
}

const PROVIDER_ERROR_MESSAGES: Record<SocialProvider, string> = {
  KAKAO: '카카오 인증 정보가 없습니다. 다시 로그인해 주세요.',
  NAVER: '네이버 인증 정보가 없습니다. 다시 로그인해 주세요.',
  FACEBOOK: '페이스북 인증 정보가 없습니다. 다시 로그인해 주세요.',
  APPLE: '애플 인증 정보가 없습니다. 다시 로그인해 주세요.',
}

export default function AuthSignupSocialPhoneVerificationStep({
  provider,
  tempToken,
  onLinked,
  onNeedsSignUp,
}: Props) {
  const router = useRouter()

  const [phoneError, setPhoneError] = useState<string | undefined>()
  const [isProcessing, startProcessing] = useTransition()

  const {
    phone,
    setPhone,
    verifyCode,
    setVerifyCode,
    isCodeVisible,
    isVerified,
    isSending,
    isConfirming,
    handleSendCode: sendCode,
    handleConfirmCode,
    resetVerified,
  } = usePhoneAuth({
    onVerified: (phoneVerifyToken) => {
      const verifiedPhone = phone.replace(/-/g, '')
      startProcessing(async () => {
        const result = await socialLinkAccountAction(provider, tempToken, phoneVerifyToken)

        if (!result.success) {
          toast(result.error)
          resetVerified()
          return
        }

        if (result.status === 'LOGIN') {
          onLinked({ phone: verifiedPhone, phoneVerifyToken })
          return
        }

        if (result.status === 'NEEDS_SIGN_UP') {
          onNeedsSignUp({
            tempToken,
            socialProfile: result.socialProfile,
            phone: verifiedPhone,
            phoneVerifyToken,
          })
          return
        }

        toast(PROVIDER_ERROR_MESSAGES[provider])
        router.replace(PAGE_PATHS.AUTH_LOGIN)
      })
    },
  })

  const handleSendCode = () => {
    const rawPhone = phone.replace(/-/g, '')
    if (!PHONE_REGEX.test(rawPhone)) {
      setPhoneError(PHONE_ERROR_MESSAGES.INVALID)
      return
    }
    setPhoneError(undefined)
    sendCode()
  }

  return (
    <div className="px-[15px] py-[30px] flex flex-col gap-5">
      <AppFormField label="휴대폰 번호" required error={phoneError}>
        {({ className }) => (
          <div className="flex flex-col gap-2">
            <div className="flex gap-2">
              <AppInputPhone
                value={phone}
                onChange={(e) => {
                  if (e.target.value.length <= 11) {
                    setPhone(e.target.value)
                  }
                  if (phoneError) setPhoneError(undefined)
                }}
                readOnly={isVerified || isProcessing}
                placeholder="숫자만 입력해 주세요."
                maxLength={11}
                className={cn('flex-1 pr-4', className)}
              />
              <AppOutlineButton
                type="button"
                onClick={handleSendCode}
                disabled={!phone.trim() || isVerified || isSending}
                className="shrink-0"
              >
                {isSending ? '발송 중' : isCodeVisible ? '재발송' : '인증번호 받기'}
              </AppOutlineButton>
            </div>
            {isCodeVisible && (
              <div className="flex gap-2">
                <AppInputPhone
                  value={verifyCode}
                  onChange={(e) => {
                    if (e.target.value.length <= 6) setVerifyCode(e.target.value)
                  }}
                  readOnly={isVerified || isProcessing}
                  placeholder="숫자만 입력해 주세요."
                  maxLength={6}
                  className="flex-1 pr-4"
                />
                <AppOutlineButton
                  type="button"
                  className="shrink-0"
                  onClick={handleConfirmCode}
                  disabled={isVerified || isConfirming || isProcessing}
                >
                  {isConfirming || isProcessing ? '확인 중' : '확인'}
                </AppOutlineButton>
              </div>
            )}
            {isVerified && !isProcessing && (
              <p className="text-xs leading-[12px] text-[#999999]">인증이 완료되었습니다.</p>
            )}
          </div>
        )}
      </AppFormField>
    </div>
  )
}
