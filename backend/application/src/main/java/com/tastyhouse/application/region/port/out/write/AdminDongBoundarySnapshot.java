package com.tastyhouse.application.region.port.out.write;

import java.math.BigDecimal;

public record AdminDongBoundarySnapshot(
    String encodedRings,
    BigDecimal minLatitude,
    BigDecimal maxLatitude,
    BigDecimal minLongitude,
    BigDecimal maxLongitude
) {
}
