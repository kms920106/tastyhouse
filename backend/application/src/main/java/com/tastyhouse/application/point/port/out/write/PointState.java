package com.tastyhouse.application.point.port.out.write;

public record PointState(
    Long id,
    Long memberId,
    Integer availablePoints,
    Integer expiredThisMonth
) {
}
