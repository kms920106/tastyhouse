package com.tastyhouse.core.domain.point.application.dto.command;

public record RefundPointCommand(
    Long memberId,
    int pointAmount
) {}
