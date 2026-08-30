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

/**
 * 행정동 마스터 어댑터.
 *
 * <p>쓰기는 {@link #synchronize}(동기화 배치 전용) 하나뿐이다 — 건별 저장 경로는 없다.
 *
 * <p><b>모든 조회가 {@code is_active = 1}로 통일돼 있다.</b> 과거 이 어댑터의 {@code existsById}와
 * {@code findByDongNameMatch}는 활성 여부를 거르지 않는 반면 {@code AdminDongQueryDao}는 걸러, 통폐합돼
 * 폐지된 행정동이 <b>검색 목록에는 안 뜨는데 등록 검증은 통과하고 주소 매칭에도 걸리는</b> 비대칭이 있었다.
 * 폐지 동은 시드가 삭제하지 않고 {@code is_active = 0}으로 남기므로(다른 테이블이 id로 참조 중이다) 이
 * 필터가 유일한 방어선이다.
 */
@Repository
public class AdminDongRepositoryImpl implements AdminDongRepository {

    private final AdminDongJpaRepository adminDongJpaRepository;

    public AdminDongRepositoryImpl(AdminDongJpaRepository adminDongJpaRepository) {
        this.adminDongJpaRepository = adminDongJpaRepository;
    }

    /**
     * 한 번에 flush 하는 행 수. 3,500여 건을 한 영속성 컨텍스트에 쌓으면 경계 문자열(행당 평균 4KB,
     * 최대 64KB)까지 함께 메모리에 머물러 힙이 불필요하게 커진다.
     */
    private static final int SAVE_BATCH_SIZE = 500;

    @Override
    public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
        if (adminDongs.isEmpty()) {
            // 원천을 못 읽었을 때 마스터를 비워버리지 않도록 막는다 — 비면 전국 배달지역이 통째로 죽는다.
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
            // managed 엔티티라 값 복사만으로 갱신된다(id 보존).
            AdminDongMapper.applyChanges(existing, adminDong);
            updated++;
        }

        int deactivated = deactivateMissing(existingByCode, sourceCodes);
        saveInChunks(inserts);
        adminDongJpaRepository.flush();

        return AdminDongSyncResult.of(inserts.size(), updated, deactivated);
    }

    /** 원천에서 사라진 행정동을 폐지 처리한다. 이미 폐지된 행은 다시 세지 않는다. */
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
