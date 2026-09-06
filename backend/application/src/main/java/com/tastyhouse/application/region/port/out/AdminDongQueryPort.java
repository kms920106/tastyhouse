package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface AdminDongQueryPort {

    PageResult<AdminDongItemResult> findAdminDongPage(String keyword, PageQuery pageQuery);

    List<AdminDongTreeItemResult> findSidoNames();

    List<AdminDongTreeItemResult> findSigunguNames(String sidoName);

    List<AdminDongTreeItemResult> findDongs(String sidoName, String sigunguName);

    List<AdminDongBoundaryResult> findBoundariesWithinBoundingBox(BigDecimal minLatitude, BigDecimal maxLatitude, BigDecimal minLongitude, BigDecimal maxLongitude, int limit);

    List<AdminDongBoundaryResult> findBoundariesByIds(Collection<Long> adminDongIds);

    List<AdminDongCandidateResult> findCandidatesWithinBoundingBox(BigDecimal minLatitude, BigDecimal maxLatitude, BigDecimal minLongitude, BigDecimal maxLongitude);
}
