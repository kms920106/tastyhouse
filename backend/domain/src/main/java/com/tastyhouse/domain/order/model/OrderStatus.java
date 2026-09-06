package com.tastyhouse.domain.order.model;

import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    COMPLETED,
    CANCELLED;

    public static OrderStatus from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_UNKNOWN,
                ErrorCode.ORDER_STATUS_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<OrderStatus> allowedTargets() {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(PREPARING, CANCELLED);
            case PREPARING -> Set.of(COMPLETED);
            case COMPLETED, CANCELLED -> Set.of();
        };
    }
}
