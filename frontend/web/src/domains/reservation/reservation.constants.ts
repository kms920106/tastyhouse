import type { ReservationErrorCode, ReservationStatus } from './reservation.types'

const RESERVATION_STATUS_NAMES: Record<ReservationStatus, string> = {
  PENDING: '예약 대기',
  CONFIRMED: '예약 확정',
  REJECTED: '예약 거절',
  CANCELED: '예약 취소',
  COMPLETED: '방문 완료',
}

const RESERVATION_STATUS_COLORS: Record<ReservationStatus, string> = {
  PENDING: '#999999',
  CONFIRMED: '#4f9857',
  REJECTED: '#bc4040',
  CANCELED: '#bc4040',
  COMPLETED: '#4f9857',
}

export const getReservationStatusName = (status: ReservationStatus): string => {
  return RESERVATION_STATUS_NAMES[status] || status
}

export const getReservationStatusColor = (status: ReservationStatus): string => {
  return RESERVATION_STATUS_COLORS[status] || '#999999'
}

// 예약 생성 에러 코드 → 사용자 노출 메시지 (백엔드 message가 없을 때의 폴백)
const RESERVATION_ERROR_MESSAGES: Record<ReservationErrorCode, string> = {
  DUPLICATE_RESERVATION:
    '이미 해당 날짜에 예약이 존재합니다. 예약을 변경하려면 기존 예약을 취소해주세요.',
  RESERVATION_SLOT_FULL: '해당 시간대는 예약이 마감되었습니다.',
  RESERVATION_TERMS_NOT_AGREED: '필수 약관에 동의해야 예약할 수 있습니다.',
  RESERVATION_INVALID_TIME: '예약할 수 없는 시간입니다.',
  RESERVATION_PAST_NOT_ALLOWED: '지난 일시는 예약할 수 없습니다.',
  SHOP_NOT_FOUND: '존재하지 않는 가게입니다.',
}

export const getReservationErrorMessage = (errorCode: string | undefined): string | null => {
  if (!errorCode) return null
  return RESERVATION_ERROR_MESSAGES[errorCode as ReservationErrorCode] ?? null
}
