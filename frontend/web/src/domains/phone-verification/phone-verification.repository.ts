import 'server-only'

import { api } from '@/lib/api'
import {
  ConfirmVerificationCodeRequest,
  PhoneVerifyTokenResponse,
  SendVerificationCodeRequest,
} from './phone-verification.dto'

const ENDPOINT = '/api/phone-verifications'

export const phoneVerificationRepository = {
  // 인증번호 발송 (POST /v1/send)
  async sendVerificationCode(data: SendVerificationCodeRequest) {
    return api.post<void>(`${ENDPOINT}/v1/send`, data)
  },
  // 인증번호 확인 (POST /v1/confirm)
  async confirmVerificationCode(data: ConfirmVerificationCodeRequest) {
    return api.post<PhoneVerifyTokenResponse>(`${ENDPOINT}/v1/confirm`, data)
  },
}
