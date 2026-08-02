'use client'

import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import {
  checkPhoneAvailability,
  confirmPhoneVerificationCode,
  sendPhoneVerificationCode,
} from '@/actions/phone-verification'
import { useState, useTransition } from 'react'

interface UsePhoneVerificationOptions {
  /**
   * 현재 등록된 번호 — 설정 시 동일 번호로 인증 시도를 차단합니다.
   * AccountInfoEditForm처럼 기존 번호 변경 여부를 검증할 때 사용합니다.
   */
  originalPhone?: string
  /**
   * 초기 전화번호 — 카카오 프로필 등 외부에서 전화번호를 미리 채울 때 사용합니다.
   */
  initialPhone?: string
}

export interface UsePhoneVerificationReturn {
  phone: string
  setPhone: (phone: string) => void
  verifyCode: string
  setVerifyCode: (code: string) => void
  isCodeVisible: boolean
  isVerified: boolean
  phoneVerifyToken: string
  isSending: boolean
  isConfirming: boolean
  handleSendCode: (onValidate?: () => string | undefined) => void
  handleConfirmCode: () => void
  reset: () => void
}

export function usePhoneVerification(
  options: UsePhoneVerificationOptions = {},
): UsePhoneVerificationReturn {
  const { originalPhone, initialPhone } = options

  const [phone, setPhone] = useState(initialPhone ?? '')
  const [verifyCode, setVerifyCode] = useState('')
  const [isCodeVisible, setIsCodeVisible] = useState(false)
  const [isVerified, setIsVerified] = useState(false)
  const [phoneVerifyToken, setPhoneVerifyToken] = useState('')
  const [isSending, startSending] = useTransition()
  const [isConfirming, startConfirming] = useTransition()

  /**
   * @param onValidate 호출 측에서 추가 검증이 필요한 경우 사용.
   *   에러 메시지를 반환하면 발송을 중단합니다.
   */
  const handleSendCode = (onValidate?: () => string | undefined) => {
    const rawPhone = phone.replace(/-/g, '')

    if (!rawPhone.match(/^01[0-9]{8,9}$/)) return

    if (originalPhone && rawPhone === originalPhone.replace(/-/g, '')) return

    const validationError = onValidate?.()
    if (validationError) return

    startSending(async () => {
      try {
        const availabilityResponse = await checkPhoneAvailability(rawPhone)
        if (availabilityResponse?.error) {
          toast(availabilityResponse.error)
          return
        }
        if (!availabilityResponse?.data?.available) {
          toast('이미 가입된 휴대폰번호입니다.')
          return
        }

        const response = await sendPhoneVerificationCode({ phoneNumber: rawPhone })
        if (response?.error) {
          toast(response.error)
          return
        }
        setIsCodeVisible(true)
        toast('인증번호가 발송되었습니다.')
      } catch {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  const handleConfirmCode = () => {
    const rawPhone = phone.replace(/-/g, '')

    startConfirming(async () => {
      try {
        const { error, data } = await confirmPhoneVerificationCode({
          phoneNumber: rawPhone,
          verificationCode: verifyCode,
        })

        if (error) {
          toast(COMMON_ERROR_MESSAGES.API_FETCH_ERROR)
          return
        }

        if (!data) {
          toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
          return
        }

        const { phoneVerifyToken } = data

        if (!phoneVerifyToken) {
          toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
          return
        }

        setPhoneVerifyToken(phoneVerifyToken)
        setIsVerified(true)
      } catch {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  const reset = () => {
    setPhone('')
    setVerifyCode('')
    setIsCodeVisible(false)
    setIsVerified(false)
    setPhoneVerifyToken('')
  }

  return {
    phone,
    setPhone,
    verifyCode,
    setVerifyCode,
    isCodeVisible,
    isVerified,
    phoneVerifyToken,
    isSending,
    isConfirming,
    handleSendCode,
    handleConfirmCode,
    reset,
  }
}
