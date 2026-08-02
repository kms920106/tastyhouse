import 'server-only'

import { api } from '@/lib/api'
import type {
  ReservationAvailabilityResponse,
  ReservationCompleteResponse,
  ReservationCreateRequest,
  ReservationDetailResponse,
  ReservationResponse,
} from './reservation.dto'

const RESERVATION_ENDPOINT = '/api/reservations'

export const reservationRepository = {
  async getAvailability(shopId: number, date: string) {
    return api.get<ReservationAvailabilityResponse, { shopId: number; date: string }>(
      `${RESERVATION_ENDPOINT}/v1/availability`,
      { params: { shopId, date } },
    )
  },

  async createReservation(request: ReservationCreateRequest) {
    return api.post<ReservationResponse>(`${RESERVATION_ENDPOINT}/v1`, request)
  },

  async getReservationComplete(reservationId: number) {
    return api.get<ReservationCompleteResponse>(
      `${RESERVATION_ENDPOINT}/v1/${reservationId}/complete`,
    )
  },

  async getReservationDetail(reservationId: number) {
    return api.get<ReservationDetailResponse>(`${RESERVATION_ENDPOINT}/v1/${reservationId}`)
  },

  async cancelReservation(reservationId: number) {
    return api.patch<void>(`${RESERVATION_ENDPOINT}/v1/${reservationId}/cancel`)
  },
}
