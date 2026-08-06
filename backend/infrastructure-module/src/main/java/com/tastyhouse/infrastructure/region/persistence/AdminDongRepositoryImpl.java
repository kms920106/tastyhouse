package com.tastyhouse.infrastructure.region.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;

/**
 * 행정동 마스터 조회 어댑터.
 *
 * <p>read-only 마스터라 저장·삭제 경로가 없다 — 행정동 목록은 행정표준코드 시드 SQL이 소유한다.
 */
@Repository
public class AdminDongRepositoryImpl implements AdminDongRepository {

    private final AdminDongJpaRepository adminDongJpaRepository;

    public AdminDongRepositoryImpl(AdminDongJpaRepository adminDongJpaRepository) {
        this.adminDongJpaRepository = adminDongJpaRepository;
    }

    @Override
    public Optional<AdminDong> findById(AdminDongId adminDongId) {
        return adminDongJpaRepository.findById(adminDongId.value())
            .map(AdminDongMapper::toDomain);
    }

    @Override
    public boolean existsById(AdminDongId adminDongId) {
        return adminDongJpaRepository.existsById(adminDongId.value());
    }

    @Override
    public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
        return adminDongJpaRepository.findBySidoNameAndSigunguNameAndDongName(sidoName, sigunguName, dongName)
            .map(AdminDongMapper::toDomain);
    }
}
