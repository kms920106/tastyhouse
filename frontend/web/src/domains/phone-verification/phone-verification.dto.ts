export interface SendVerificationCodeRequest {
  phoneNumber: string
}

export interface ConfirmVerificationCodeRequest {
  phoneNumber: string
  verificationCode: string
}

export interface PhoneVerifyTokenResponse {
  phoneVerifyToken: string
}
