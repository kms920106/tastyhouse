package com.tastyhouse.infrastructure.banner.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.repository.BannerRepository;
import com.tastyhouse.domain.banner.vo.BannerId;

/**
 * 배너 write 어댑터.
 *
 * <p>도메인 모델을 주고받는 CRUD만 담당한다. 표현 목적 조회는 같은 모듈의
 * {@code banner/query/BannerQueryDao}로 분리되어 있어 이 클래스는 QueryDSL을 쓰지 않는다.
 */
@Repository
public class BannerRepositoryImpl implements BannerRepository {

    private final BannerJpaRepository bannerJpaRepository;

    public BannerRepositoryImpl(BannerJpaRepository bannerJpaRepository) {
        this.bannerJpaRepository = bannerJpaRepository;
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findByIdAndDeletedFalse(id.value())
            .map(BannerMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerMapper.toEntity(banner));
            return BannerMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        BannerJpaEntity entity = bannerJpaRepository.findById(banner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId()));
        BannerMapper.applyChanges(entity, banner);
        return BannerMapper.toDomain(entity);
    }
}
