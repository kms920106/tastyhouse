package com.tastyhouse.domain.region.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

public interface AdminDongRepository {
    AdminDongSyncResult synchronize(List<AdminDong> adminDongs);

    Optional<AdminDong> findById(AdminDongId adminDongId);

    boolean existsById(AdminDongId adminDongId);

    Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName);

    List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox);

    List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds);

    Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds);
}
