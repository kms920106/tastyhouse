package com.tastyhouse.infrastructure.persistence.banner.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.out.write.BannerPersistencePort;

import static com.tastyhouse.infrastructure.persistence.banner.persistence.QBannerJpaEntity.bannerJpaEntity;

@Repository
@ConditionalOnProperty(name = "persistence.banner.write.provider", havingValue = "jpa", matchIfMissing = true)
class BannerJpaPersistenceAdapter implements BannerPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final BannerJpaRepository bannerJpaRepository;

    public BannerJpaPersistenceAdapter(JPAQueryFactory queryFactory, BannerJpaRepository bannerJpaRepository) {
        this.queryFactory = queryFactory;
        this.bannerJpaRepository = bannerJpaRepository;
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        BannerJpaEntity entity = queryFactory
            .selectFrom(bannerJpaEntity)
            .where(
                bannerJpaEntity.id.eq(id.value()),
                bannerJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(BannerJpaMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerJpaMapper.toEntity(banner));
            return BannerJpaMapper.toDomain(saved);
        }

        BannerJpaEntity entity = bannerJpaRepository.findById(banner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId()));
        BannerJpaMapper.applyChanges(entity, banner);
        return BannerJpaMapper.toDomain(entity);
    }
}
