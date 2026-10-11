package com.tastyhouse.application.point.port.out;

public record PointBalanceResult(
    Integer availablePoints,
    Integer expiredThisMonth
) {
}
