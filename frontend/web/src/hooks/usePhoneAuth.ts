'use client'

import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import {
  confirmPhoneVerificationCode,
  sendPhoneVerificationCode,
} from '@/actions/phone-verification'
import { useState, useTransition } from 'react'

export interface UsePhoneAuthOptions {
  onVerified?: (phoneVerifyToken: string) => void
}

export interface UsePhoneAuthReturn {
  phone: string
  setPhone: (phone: string) => void
  verifyCode: string
  setVerifyCode: (code: string) => void
  isCodeVisible: boolean
  isVerified: boolean
  phoneVerifyToken: string
  isSending: boolean
  isConfirming: boolean
  handleSendCode: () => void
  handleConfirmCode: () => void
  resetVerified: () => void
}

export function usePhoneAuth(options?: UsePhoneAuthOptions): UsePhoneAuthReturn {
  const [phone, setPhone] = useState('')
  const [verifyCode, setVerifyCode] = useState('')
  const [isCodeVisible, setIsCodeVisible] = useState(false)
  const [isVerified, setIsVerified] = useState(false)
  const [phoneVerifyToken, setPhoneVerifyToken] = useState('')
  const [isSending, startSending] = useTransition()
  const [isConfirming, startConfirming] = useTransition()

  const handleSendCode = () => {
    const rawPhone = phone.replace(/-/g, '')
    if (!rawPhone.match(/^01[0-9]{8,9}$/)) return

    startSending(async () => {
      try {
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
        options?.onVerified?.(phoneVerifyToken)
      } catch {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  const resetVerified = () => {
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
    resetVerified,
  }
}
