package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;
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

import com.tastyhouse.application.region.port.out.write.AdminDongState;
import com.tastyhouse.application.region.port.out.write.AdminDongStatePort;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;

@Repository
public class AdminDongStatePortImpl implements AdminDongStatePort {
    private final AdminDongJpaRepository adminDongJpaRepository;

    public AdminDongStatePortImpl(AdminDongJpaRepository adminDongJpaRepository) {
        this.adminDongJpaRepository = adminDongJpaRepository;
    }

    private static final int SAVE_BATCH_SIZE = 500;

    @Override
    public AdminDongSyncResult synchronize(List<AdminDongState> adminDongs) {
        if (adminDongs.isEmpty()) {
            throw new IllegalArgumentException("행정동 마스터를 빈 목록으로 동기화할 수 없습니다.");
        }

        Map<String, AdminDongJpaEntity> existingByCode = adminDongJpaRepository.findAll().stream()
            .collect(Collectors.toMap(AdminDongJpaEntity::getCode, entity -> entity, (a, b) -> a));

        List<AdminDongJpaEntity> inserts = new ArrayList<>();
        Set<String> sourceCodes = new HashSet<>();
        int updated = 0;

        for (AdminDongState adminDong : adminDongs) {
            sourceCodes.add(adminDong.code());

            AdminDongJpaEntity existing = existingByCode.get(adminDong.code());
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
    public Optional<AdminDongState> findById(Long id) {
        return adminDongJpaRepository.findById(id)
            .map(AdminDongMapper::toState);
    }

    @Override
    public boolean existsById(Long id) {
        return adminDongJpaRepository.existsByIdAndActiveIsTrue(id);
    }

    @Override
    public Optional<AdminDongState> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
        return adminDongJpaRepository
            .findBySidoNameAndSigunguNameAndDongNameAndActiveIsTrue(sidoName, sigunguName, dongName)
            .map(AdminDongMapper::toState);
    }

    @Override
    public List<AdminDongState> findAllWithinBoundingBox(
        BigDecimal minLatitude,
        BigDecimal maxLatitude,
        BigDecimal minLongitude,
        BigDecimal maxLongitude
    ) {
        return adminDongJpaRepository.findAllWithinBoundingBox(
            minLatitude,
            maxLatitude,
            minLongitude,
            maxLongitude
        ).stream().map(AdminDongMapper::toState).toList();
    }

    @Override
    public List<AdminDongState> findAllByIds(Collection<Long> ids) {
        return adminDongJpaRepository.findByIdInAndActiveIsTrue(ids).stream()
            .map(AdminDongMapper::toState)
            .toList();
    }

    @Override
    public Set<Long> filterExistingIds(Collection<Long> ids) {
        return new LinkedHashSet<>(adminDongJpaRepository.findExistingIds(ids));
    }
}
