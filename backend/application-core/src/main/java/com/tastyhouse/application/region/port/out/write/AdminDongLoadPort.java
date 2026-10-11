package com.tastyhouse.application.region.port.out.write;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

public interface AdminDongLoadPort {

    Optional<AdminDong> findById(AdminDongId adminDongId);

    boolean existsActiveById(AdminDongId adminDongId);

    Optional<AdminDong> findActiveByDongNameMatch(String sidoName, String sigunguName, String dongName);

    List<AdminDong> findAllActiveWithinBoundingBox(GeoBoundingBox boundingBox);

    List<AdminDong> findAllActiveByIds(Collection<AdminDongId> adminDongIds);

    Set<AdminDongId> filterActiveIds(Collection<AdminDongId> adminDongIds);
}
