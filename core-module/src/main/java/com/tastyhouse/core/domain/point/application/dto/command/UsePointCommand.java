package com.tastyhouse.core.domain.point.application.dto.command;

public record UsePointCommand(
    Long memberId,
    int pointAmount
) {

    public static UsePointCommand of(Long memberId, int pointAmount) {
        return new UsePointCommand(memberId, pointAmount);
    }
}
