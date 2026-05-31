package com.tastyhouse.core.domain.reservation.domain.model;

public enum ReservationStatus {
    PENDING,    // 예약 대기 (사용자 신청, 점주 승인 전)
    CONFIRMED,  // 예약 확정 (점주 승인)
    REJECTED,   // 예약 거절 (점주)
    CANCELED,   // 예약 취소 (사용자)
    COMPLETED   // 방문 완료
}
