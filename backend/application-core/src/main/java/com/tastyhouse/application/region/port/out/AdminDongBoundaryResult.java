package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;

public record AdminDongBoundaryResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    String boundary
) {
}
