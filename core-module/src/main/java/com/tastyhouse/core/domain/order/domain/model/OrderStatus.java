package com.tastyhouse.core.domain.order.domain.model;

public enum OrderStatus {
    PENDING,        // 주문 대기
    CONFIRMED,      // 주문 확인
    PREPARING,      // 준비 중
    COMPLETED,      // 완료
    CANCELLED       // 취소
}
