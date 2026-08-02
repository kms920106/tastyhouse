'use client'

import ForgotPasswordEmailStep from '@/app/auth/forgot-password/_components/ForgotPasswordEmailStep'
import ForgotPasswordResetStep from '@/app/auth/forgot-password/_components/ForgotPasswordResetStep'
import { useState } from 'react'
import AuthForgotPasswordHeader from './AuthForgotPasswordHeader'

export default function AuthForgotPasswordPage() {
  const [passwordResetToken, setPasswordResetToken] = useState<string | null>(null)

  return (
    <>
      <AuthForgotPasswordHeader />
      {passwordResetToken ? (
        <ForgotPasswordResetStep passwordResetToken={passwordResetToken} />
      ) : (
        <ForgotPasswordEmailStep onVerified={(_, token) => setPasswordResetToken(token)} />
      )}
    </>
  )
}
