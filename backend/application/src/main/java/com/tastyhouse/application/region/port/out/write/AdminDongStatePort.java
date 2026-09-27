package com.tastyhouse.application.region.port.out.write;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdminDongStatePort {
    AdminDongSyncResult synchronize(List<AdminDongState> adminDongs);

    Optional<AdminDongState> findById(Long id);

    boolean existsById(Long id);

    Optional<AdminDongState> findByDongNameMatch(String sidoName, String sigunguName, String dongName);

    List<AdminDongState> findAllWithinBoundingBox(
        BigDecimal minLatitude,
        BigDecimal maxLatitude,
        BigDecimal minLongitude,
        BigDecimal maxLongitude
    );

    List<AdminDongState> findAllByIds(Collection<Long> ids);

    Set<Long> filterExistingIds(Collection<Long> ids);
}
