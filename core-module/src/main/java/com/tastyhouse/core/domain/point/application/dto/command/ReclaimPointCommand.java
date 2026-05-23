package com.tastyhouse.core.domain.point.application.dto.command;

public record ReclaimPointCommand(
    Long memberId,
    int pointAmount
) {}
