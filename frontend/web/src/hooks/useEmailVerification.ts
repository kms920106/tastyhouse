'use client'

import { toast } from '@/components/ui/AppToaster'
import { EMAIL_ERROR_MESSAGES, EMAIL_REGEX } from '@/constants/validation'
import { useState, useTransition } from 'react'

type SendResult = { error?: string } | null
type ConfirmResult = { error?: string; data?: { token: string } } | null

interface UseEmailVerificationOptions {
  sendFn: (email: string) => Promise<SendResult>
  confirmFn: (email: string, code: string) => Promise<ConfirmResult>
}

export interface UseEmailVerificationReturn {
  email: string
  setEmail: (email: string) => void
  verifyCode: string
  setVerifyCode: (code: string) => void
  isCodeVisible: boolean
  isVerified: boolean
  token: string
  isSending: boolean
  isConfirming: boolean
  handleSendCode: () => void
  handleConfirmCode: () => void
  reset: () => void
}

export function useEmailVerification({
  sendFn,
  confirmFn,
}: UseEmailVerificationOptions): UseEmailVerificationReturn {
  const [email, setEmailState] = useState('')
  const [verifyCode, setVerifyCode] = useState('')
  const [isCodeVisible, setIsCodeVisible] = useState(false)
  const [isVerified, setIsVerified] = useState(false)
  const [token, setToken] = useState('')
  const [isSending, startSending] = useTransition()
  const [isConfirming, startConfirming] = useTransition()

  const setEmail = (value: string) => {
    setEmailState(value)
    setIsVerified(false)
    setIsCodeVisible(false)
    setToken('')
  }

  const handleSendCode = () => {
    if (!email.trim()) {
      toast(EMAIL_ERROR_MESSAGES.REQUIRED)
      return
    }
    if (!EMAIL_REGEX.test(email)) {
      toast(EMAIL_ERROR_MESSAGES.INVALID)
      return
    }
    startSending(async () => {
      try {
        const response = await sendFn(email)
        if (response?.error) {
          toast(response.error)
          return
        }
        setIsCodeVisible(true)
        toast('인증 메일이 발송되었습니다.')
      } catch {
        toast('인증 메일 발송에 실패했습니다. 다시 시도해 주세요.')
      }
    })
  }

  const handleConfirmCode = () => {
    startConfirming(async () => {
      try {
        const response = await confirmFn(email, verifyCode)
        if (response?.error) {
          toast(response.error)
          return
        }
        const receivedToken = response?.data?.token
        if (!receivedToken) {
          toast('인증에 실패했습니다. 다시 시도해 주세요.')
          return
        }
        setToken(receivedToken)
        setIsVerified(true)
      } catch {
        toast('인증번호 확인에 실패했습니다. 다시 시도해 주세요.')
      }
    })
  }

  const reset = () => {
    setEmailState('')
    setVerifyCode('')
    setIsCodeVisible(false)
    setIsVerified(false)
    setToken('')
  }

  return {
    email,
    setEmail,
    verifyCode,
    setVerifyCode,
    isCodeVisible,
    isVerified,
    token,
    isSending,
    isConfirming,
    handleSendCode,
    handleConfirmCode,
    reset,
  }
}
