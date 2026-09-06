package com.tastyhouse.infrastructure.region.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

@Repository
public class AdminDongRepositoryImpl implements AdminDongRepository {
    private final AdminDongJpaRepository adminDongJpaRepository;

    public AdminDongRepositoryImpl(AdminDongJpaRepository adminDongJpaRepository) {
        this.adminDongJpaRepository = adminDongJpaRepository;
    }

    private static final int SAVE_BATCH_SIZE = 500;

    @Override
    public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
        if (adminDongs.isEmpty()) {
            throw new IllegalArgumentException("행정동 마스터를 빈 목록으로 동기화할 수 없습니다.");
        }

        Map<String, AdminDongJpaEntity> existingByCode = adminDongJpaRepository.findAll().stream()
            .collect(Collectors.toMap(AdminDongJpaEntity::getCode, entity -> entity, (a, b) -> a));

        List<AdminDongJpaEntity> inserts = new ArrayList<>();
        Set<String> sourceCodes = new HashSet<>();
        int updated = 0;

        for (AdminDong adminDong : adminDongs) {
            sourceCodes.add(adminDong.getCode());

            AdminDongJpaEntity existing = existingByCode.get(adminDong.getCode());
            if (existing == null) {
                inserts.add(AdminDongMapper.toEntity(adminDong));
                continue;
            }
            AdminDongMapper.applyChanges(existing, adminDong);
            updated++;
        }

        int deactivated = deactivateMissing(existingByCode, sourceCodes);
        saveInChunks(inserts);
        adminDongJpaRepository.flush();

        return AdminDongSyncResult.of(inserts.size(), updated, deactivated);
    }

    private int deactivateMissing(Map<String, AdminDongJpaEntity> existingByCode, Set<String> sourceCodes) {
        int deactivated = 0;
        for (Map.Entry<String, AdminDongJpaEntity> entry : existingByCode.entrySet()) {
            AdminDongJpaEntity entity = entry.getValue();
            if (sourceCodes.contains(entry.getKey()) || !entity.isActive()) {
                continue;
            }
            entity.deactivate();
            deactivated++;
        }
        return deactivated;
    }

    private void saveInChunks(List<AdminDongJpaEntity> entities) {
        for (int start = 0; start < entities.size(); start += SAVE_BATCH_SIZE) {
            int end = Math.min(start + SAVE_BATCH_SIZE, entities.size());
            adminDongJpaRepository.saveAll(entities.subList(start, end));
            adminDongJpaRepository.flush();
        }
    }

    @Override
    public Optional<AdminDong> findById(AdminDongId adminDongId) {
        return adminDongJpaRepository.findById(adminDongId.value())
            .map(AdminDongMapper::toDomain);
    }

    @Override
    public boolean existsById(AdminDongId adminDongId) {
        return adminDongJpaRepository.existsByIdAndActiveIsTrue(adminDongId.value());
    }

    @Override
    public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
        return adminDongJpaRepository
            .findBySidoNameAndSigunguNameAndDongNameAndActiveIsTrue(sidoName, sigunguName, dongName)
            .map(AdminDongMapper::toDomain);
    }

    @Override
    public List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox) {
        return adminDongJpaRepository.findAllWithinBoundingBox(
            boundingBox.minLatitude(),
            boundingBox.maxLatitude(),
            boundingBox.minLongitude(),
            boundingBox.maxLongitude()
        ).stream().map(AdminDongMapper::toDomain).toList();
    }

    @Override
    public List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds) {
        if (adminDongIds.isEmpty()) {
            return List.of();
        }

        return adminDongJpaRepository.findByIdInAndActiveIsTrue(rawIds(adminDongIds)).stream()
            .map(AdminDongMapper::toDomain)
            .toList();
    }

    @Override
    public Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds) {
        if (adminDongIds.isEmpty()) {
            return Set.of();
        }

        return adminDongJpaRepository.findExistingIds(rawIds(adminDongIds)).stream()
            .map(AdminDongId::of)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<Long> rawIds(Collection<AdminDongId> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::value).toList();
    }
}
