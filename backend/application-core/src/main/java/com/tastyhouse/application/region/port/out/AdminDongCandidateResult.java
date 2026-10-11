package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;

public record AdminDongCandidateResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    String boundary
) {
}
