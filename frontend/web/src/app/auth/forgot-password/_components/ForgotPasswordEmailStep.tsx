'use client'

import {
  requestPasswordReset,
  verifyPasswordResetForEmailField,
} from '@/actions/auth'
import EmailVerificationField from '@/components/ui/EmailVerificationField'
import { useEmailVerification } from '@/hooks/useEmailVerification'
import { useEffect } from 'react'

interface Props {
  onVerified: (email: string, passwordResetToken: string) => void
}

export default function ForgotPasswordEmailStep({ onVerified }: Props) {
  const emailVerification = useEmailVerification({
    sendFn: requestPasswordReset,
    confirmFn: verifyPasswordResetForEmailField,
  })

  useEffect(() => {
    if (emailVerification.isVerified) {
      onVerified(emailVerification.email, emailVerification.token)
    }
  }, [emailVerification.isVerified, emailVerification.email, emailVerification.token, onVerified])

  return (
    <div className="px-[15px] py-[30px] flex flex-col gap-5">
      <EmailVerificationField verification={emailVerification} label="아이디" />
    </div>
  )
}
