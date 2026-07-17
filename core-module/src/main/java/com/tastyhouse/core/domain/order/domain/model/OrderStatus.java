package com.tastyhouse.core.domain.order.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum OrderStatus {
    PENDING,        // 주문 대기
    CONFIRMED,      // 주문 확인
    PREPARING,      // 준비 중
    COMPLETED,      // 완료
    CANCELLED;      // 취소

    public static OrderStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_UNKNOWN,
                ErrorCode.ORDER_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
