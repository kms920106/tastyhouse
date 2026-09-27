package com.tastyhouse.application.region.port.out.write;

import java.math.BigDecimal;

public record AdminDongCenterSnapshot(
    BigDecimal latitude,
    BigDecimal longitude
) {
}
