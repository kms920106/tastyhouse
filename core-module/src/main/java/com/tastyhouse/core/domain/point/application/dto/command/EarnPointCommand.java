package com.tastyhouse.core.domain.point.application.dto.command;

public record EarnPointCommand(
    Long memberId,
    int pointAmount,
    String reason
) {

    public static EarnPointCommand of(Long memberId, int pointAmount, String reason) {
        return new EarnPointCommand(memberId, pointAmount, reason);
    }
}
