package com.tastyhouse.core.domain.point.application.dto.command;

public record ReclaimPointCommand(
    Long memberId,
    int pointAmount
) {

    public static ReclaimPointCommand of(Long memberId, int pointAmount) {
        return new ReclaimPointCommand(memberId, pointAmount);
    }
}
