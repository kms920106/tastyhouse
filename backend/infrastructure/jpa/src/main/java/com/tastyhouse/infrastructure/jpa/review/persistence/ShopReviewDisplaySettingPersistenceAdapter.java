package com.tastyhouse.infrastructure.jpa.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingLoadPort;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingSavePort;

import static com.tastyhouse.infrastructure.jpa.review.persistence.QShopReviewDisplaySettingJpaEntity.shopReviewDisplaySettingJpaEntity;

@Repository
class ShopReviewDisplaySettingPersistenceAdapter implements ShopReviewDisplaySettingLoadPort, ShopReviewDisplaySettingSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository;

    public ShopReviewDisplaySettingPersistenceAdapter(JPAQueryFactory queryFactory, ShopReviewDisplaySettingJpaRepository shopReviewDisplaySettingJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopReviewDisplaySettingJpaRepository = shopReviewDisplaySettingJpaRepository;
    }

    @Override
    public Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId) {
        ShopReviewDisplaySettingJpaEntity entity = queryFactory
            .selectFrom(shopReviewDisplaySettingJpaEntity)
            .where(shopReviewDisplaySettingJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ShopReviewDisplaySettingMapper::toDomain);
    }

    @Override
    public ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting) {
        if (shopReviewDisplaySetting.getId() == null) {
            ShopReviewDisplaySettingJpaEntity saved =
                shopReviewDisplaySettingJpaRepository.save(ShopReviewDisplaySettingMapper.toEntity(shopReviewDisplaySetting));
            return ShopReviewDisplaySettingMapper.toDomain(saved);
        }

        ShopReviewDisplaySettingJpaEntity entity = shopReviewDisplaySettingJpaRepository.findById(shopReviewDisplaySetting.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 노출 정렬 설정입니다: " + shopReviewDisplaySetting.getId()));
        ShopReviewDisplaySettingMapper.applyChanges(entity, shopReviewDisplaySetting);
        return ShopReviewDisplaySettingMapper.toDomain(entity);
    }
}
