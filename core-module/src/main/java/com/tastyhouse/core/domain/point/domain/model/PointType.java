package com.tastyhouse.core.domain.point.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

public enum PointType {
    EARNED,  // 적립
    USE,     // 사용
    REFUND;  // 환불

    public static PointType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.POINT_TYPE_UNKNOWN,
                ErrorCode.POINT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
