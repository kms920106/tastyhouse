export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const EMAIL_ERROR_MESSAGES = {
  REQUIRED: '이메일 주소를 입력해 주세요.',
  INVALID: '올바른 이메일 주소를 입력해 주세요.',
}

export const PHONE_REGEX = /^01[0-9]{8,9}$/

export const PHONE_ERROR_MESSAGES = {
  REQUIRED: '휴대폰 번호를 입력해 주세요.',
  INVALID: '올바른 휴대폰 번호를 입력해 주세요. ("-" 없이 숫자만 입력)',
}
