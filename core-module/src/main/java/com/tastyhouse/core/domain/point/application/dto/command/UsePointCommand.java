package com.tastyhouse.core.domain.point.application.dto.command;

public record UsePointCommand(
    Long memberId,
    int pointAmount
) {}
