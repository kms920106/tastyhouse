const TOSS_PAYMENT_USER_CANCEL_CODES = ['USER_CANCEL', 'PAY_PROCESS_CANCELED']

export function isTossPaymentUserCancel(error: unknown): boolean {
  if (typeof error !== 'object' || error === null || !('code' in error)) return false

  return TOSS_PAYMENT_USER_CANCEL_CODES.includes(String(error.code))
}
