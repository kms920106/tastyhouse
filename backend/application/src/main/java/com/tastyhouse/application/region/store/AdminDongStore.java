package com.tastyhouse.application.region.store;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.application.region.port.out.write.AdminDongStatePort;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

public class AdminDongStore implements AdminDongRepository {
    private final AdminDongStatePort adminDongStatePort;

    public AdminDongStore(AdminDongStatePort adminDongStatePort) {
        this.adminDongStatePort = adminDongStatePort;
    }

    @Override
    public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
        return adminDongStatePort.synchronize(adminDongs.stream().map(AdminDongStateMapper::toState).toList());
    }

    @Override
    public Optional<AdminDong> findById(AdminDongId adminDongId) {
        return adminDongStatePort.findById(adminDongId.value()).map(AdminDongStateMapper::toDomain);
    }

    @Override
    public boolean existsById(AdminDongId adminDongId) {
        return adminDongStatePort.existsById(adminDongId.value());
    }

    @Override
    public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
        return adminDongStatePort.findByDongNameMatch(sidoName, sigunguName, dongName)
            .map(AdminDongStateMapper::toDomain);
    }

    @Override
    public List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox) {
        return adminDongStatePort.findAllWithinBoundingBox(
            boundingBox.minLatitude(),
            boundingBox.maxLatitude(),
            boundingBox.minLongitude(),
            boundingBox.maxLongitude()
        ).stream().map(AdminDongStateMapper::toDomain).toList();
    }

    @Override
    public List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds) {
        if (adminDongIds.isEmpty()) {
            return List.of();
        }

        return adminDongStatePort.findAllByIds(rawIds(adminDongIds)).stream()
            .map(AdminDongStateMapper::toDomain)
            .toList();
    }

    @Override
    public Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds) {
        if (adminDongIds.isEmpty()) {
            return Set.of();
        }

        return adminDongStatePort.filterExistingIds(rawIds(adminDongIds)).stream()
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<Long> rawIds(Collection<AdminDongId> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::value).toList();
    }
}
