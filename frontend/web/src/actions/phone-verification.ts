'use server'

import { memberRepository } from '@/domains/member/member.repository'
import { phoneVerificationRepository } from '@/domains/phone-verification/phone-verification.repository'

export async function checkPhoneAvailability(phoneNumber: string) {
  return memberRepository.checkPhoneAvailability(phoneNumber)
}

export async function sendPhoneVerificationCode({ phoneNumber }: { phoneNumber: string }): Promise<{
  error?: string
  data?: void
}> {
  return phoneVerificationRepository.sendVerificationCode({ phoneNumber })
}

export async function confirmPhoneVerificationCode({
  phoneNumber,
  verificationCode,
}: {
  phoneNumber: string
  verificationCode: string
}): Promise<{
  error?: string
  data?: { phoneVerifyToken: string }
}> {
  return phoneVerificationRepository.confirmVerificationCode({ phoneNumber, verificationCode })
}
