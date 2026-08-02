export interface SendEmailVerificationCodeRequest {
  email: string
}

export interface ConfirmEmailVerificationCodeRequest {
  email: string
  verificationCode: string
}

export interface EmailVerifyTokenResponse {
  emailVerifyToken: string
}
