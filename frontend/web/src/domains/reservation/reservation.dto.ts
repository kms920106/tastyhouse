import type { ReservationSlot } from './reservation.model'
import type { ReservationStatus } from './reservation.types'

export interface ReservationAvailabilityResponse {
  date: string
  // 로그인 회원이 그 날짜·그 매장에 이미 차단 예약(PENDING/CONFIRMED/COMPLETED)을 가지고 있는지
  // true면 그 날짜의 모든 슬롯 available이 false로 내려온다.
  hasMyReservation: boolean
  slots: ReservationSlot[]
}

export interface ReservationCreateRequest {
  shopId: number
  reservationDate: string // YYYY-MM-DD
  reservationTime: string // HH:mm
  partySize: number // 1~50
  request?: string
  agreedRequiredTerms: boolean
}

export interface ReservationResponse {
  id: number
  shopId: number
  shopName: string
  memberId: number
  reservationDate: string
  reservationTime: string
  partySize: number
  status: ReservationStatus
  request: string | null
  createdAt: string
}

export interface ReservationCompleteResponse {
  id: number
  shopName: string
  shopImageUrl: string
  reservationAt: string
  partySize: number
}

// 예약 상세 조회 응답 (GET /api/reservations/v1/{reservationId})
export interface ReservationDetailResponse {
  id: number
  shopId: number
  shopName: string
  shopImageUrl: string | null // 가게 썸네일 이미지 URL
  shopRoadAddress: string | null // 매장 도로명 주소
  shopLotAddress: string | null // 매장 지번 주소
  memberId: number
  reserverName: string // 예약자 이름
  reserverPhoneNumber: string | null // 예약자 휴대폰 (하이픈 없는 raw 값)
  reserverEmail: string // 예약자 이메일
  reservationAt: string // YYYY-MM-DDTHH:mm:ss (LocalDateTime, 예약 일시)
  partySize: number
  status: ReservationStatus
  request: string | null
  createdAt: string // YYYY-MM-DDTHH:mm:ss (LocalDateTime, 예약 생성 일시)
}
