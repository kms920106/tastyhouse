export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'CANCELED' | 'COMPLETED'

// 예약 생성(POST /api/reservations/v1) 시 백엔드가 내려주는 비즈니스 에러 코드
export type ReservationErrorCode =
  | 'DUPLICATE_RESERVATION' // 같은 매장·날짜 중복
  | 'RESERVATION_SLOT_FULL' // 슬롯 마감/정원 초과
  | 'RESERVATION_TERMS_NOT_AGREED' // 필수 약관 미동의
  | 'RESERVATION_INVALID_TIME' // 잘못된 슬롯 시간
  | 'RESERVATION_PAST_NOT_ALLOWED' // 과거 일시
  | 'SHOP_NOT_FOUND' // 가게 없음
