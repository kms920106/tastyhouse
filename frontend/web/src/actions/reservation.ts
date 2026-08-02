'use server'

import { reservationRepository } from '@/domains/reservation/reservation.repository'

export async function getReservationAvailability(shopId: number, date: string) {
  return reservationRepository.getAvailability(shopId, date)
}

export async function getReservationComplete(reservationId: number) {
  return reservationRepository.getReservationComplete(reservationId)
}

export async function getReservationDetail(reservationId: number) {
  return reservationRepository.getReservationDetail(reservationId)
}

export type CancelReservationCode =
  | 'SUCCESS'
  | 'ALREADY_PROCESSED'
  | 'UNAUTHORIZED'
  | 'NOT_FOUND'
  | 'UNKNOWN'

export type CancelReservationResult =
  | { success: true; code: 'SUCCESS' }
  | { success: false; code: Exclude<CancelReservationCode, 'SUCCESS'> }

export async function cancelReservation(reservationId: number): Promise<CancelReservationResult> {
  const { error, status } = await reservationRepository.cancelReservation(reservationId)

  if (!error) {
    return { success: true, code: 'SUCCESS' }
  }

  // 취소 불가 상태 (이미 취소/거절/방문완료된 예약)
  if (status === 409) {
    return { success: false, code: 'ALREADY_PROCESSED' }
  }

  // 인증되지 않은 사용자 / 본인의 예약이 아님
  if (status === 401 || status === 403) {
    return { success: false, code: 'UNAUTHORIZED' }
  }

  // 예약을 찾을 수 없음
  if (status === 404) {
    return { success: false, code: 'NOT_FOUND' }
  }

  return { success: false, code: 'UNKNOWN' }
}

export async function createReservation({
  shopId,
  reservationDate,
  reservationTime,
  partySize,
  request,
  agreedRequiredTerms,
}: {
  shopId: number
  reservationDate: string
  reservationTime: string
  partySize: number
  request?: string
  agreedRequiredTerms: boolean
}) {
  return reservationRepository.createReservation({
    shopId,
    reservationDate,
    reservationTime,
    partySize,
    request,
    agreedRequiredTerms,
  })
}
