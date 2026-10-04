package com.tastyhouse.domain.point.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum PointType {

    EARNED,
    USE,
    REFUND;

    public static PointType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.POINT_TYPE_UNKNOWN,
                DomainErrorCode.POINT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
