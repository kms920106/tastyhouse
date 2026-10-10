package com.tastyhouse.infrastructure.jpa.banner.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;

import static com.tastyhouse.infrastructure.jpa.banner.persistence.QBannerJpaEntity.bannerJpaEntity;

@Repository
class BannerPersistenceAdapter implements BannerLoadPort, BannerSavePort {

    private final JPAQueryFactory queryFactory;
    private final BannerJpaRepository bannerJpaRepository;

    public BannerPersistenceAdapter(JPAQueryFactory queryFactory, BannerJpaRepository bannerJpaRepository) {
        this.queryFactory = queryFactory;
        this.bannerJpaRepository = bannerJpaRepository;
    }

    @Override
    public Optional<Banner> findActiveById(BannerId id) {
        BannerJpaEntity entity = queryFactory
            .selectFrom(bannerJpaEntity)
            .where(
                bannerJpaEntity.id.eq(id.value()),
                bannerJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(BannerMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerMapper.toEntity(banner));
            return BannerMapper.toDomain(saved);
        }

        BannerJpaEntity entity = bannerJpaRepository.findById(banner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId()));
        BannerMapper.applyChanges(entity, banner);
        return BannerMapper.toDomain(entity);
    }
}
