package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum OrderMethod {

    TABLE("테이블 오더"),
    RESERVATION("예약"),
    DELIVERY("배달"),
    TAKEOUT("포장");

    private final String displayName;

    public static OrderMethod from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_METHOD_UNKNOWN,
                ErrorCode.ORDER_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
