package com.tastyhouse.core.domain.point.application.dto.command;

public record EarnPointCommand(
    Long memberId,
    int pointAmount,
    String reason
) {}
