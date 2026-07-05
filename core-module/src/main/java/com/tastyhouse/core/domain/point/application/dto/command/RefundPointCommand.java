package com.tastyhouse.core.domain.point.application.dto.command;

public record RefundPointCommand(
    Long memberId,
    int pointAmount
) {

    public static RefundPointCommand of(Long memberId, int pointAmount) {
        return new RefundPointCommand(memberId, pointAmount);
    }
}
