package com.tastyhouse.application.region.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.application.region.port.out.AdminDongBoundariesResult;
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.region.port.out.AdminDongTreeResult;
import com.tastyhouse.domain.shared.page.PageResult;

@CeoApp
public interface AdminDongQueryUseCase {

    PageResult<AdminDongItemResult> getAdminDongs(String keyword, int page, int size);

    AdminDongTreeResult getAdminDongTree(String sidoName, String sigunguName);

    AdminDongBoundariesResult getAdminDongBoundaries(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Integer level,
        List<Long> adminDongIds
    );
}
