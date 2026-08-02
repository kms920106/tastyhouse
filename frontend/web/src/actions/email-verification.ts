'use server'

import { emailVerificationRepository } from '@/domains/email-verification/email-verification.repository'

export async function sendEmailVerificationCode(email: string) {
  return emailVerificationRepository.sendVerificationCode({ email })
}

export async function confirmEmailVerificationCode(email: string, verificationCode: string) {
  return emailVerificationRepository.confirmVerificationCode({ email, verificationCode })
}

export async function confirmEmailVerificationCodeForEmailField(
  email: string,
  verificationCode: string,
): Promise<{ error?: string; data?: { token: string } } | null> {
  const response = await emailVerificationRepository.confirmVerificationCode({
    email,
    verificationCode,
  })
  if (!response || response.error) return response as { error: string } | null
  const token = response.data?.emailVerifyToken
  return { data: { token: token ?? '' } }
}
