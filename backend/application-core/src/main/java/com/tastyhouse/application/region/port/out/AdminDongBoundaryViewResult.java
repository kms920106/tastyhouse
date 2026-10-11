package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;
import java.util.List;

public record AdminDongBoundaryViewResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    List<List<Point>> rings
) {

    public record Point(
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }
}
