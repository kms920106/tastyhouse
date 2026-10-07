package com.tastyhouse.application.region.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.application.region.port.out.AdminDongBoundariesResult;

public interface AdminDongBoundaryQueryUseCase {

    AdminDongBoundariesResult getAdminDongBoundaries(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Integer level,
        List<Long> adminDongIds
    );
}
