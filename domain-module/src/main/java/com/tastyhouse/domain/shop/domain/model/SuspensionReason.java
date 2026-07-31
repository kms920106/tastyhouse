package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum SuspensionReason {

    EARLY_CLOSE("조기종료"),
    OPEN_DELAY("오픈지연"),
    SHOP_CIRCUMSTANCE("가게사정"),
    UNREACHABLE("연락불가"),
    TERMINATION_REQUEST("해지요청"),
    BAD_WEATHER("기상악화");

    private final String description;

    public static SuspensionReason from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_SUSPENSION_REASON_UNKNOWN,
                ErrorCode.SHOP_SUSPENSION_REASON_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
