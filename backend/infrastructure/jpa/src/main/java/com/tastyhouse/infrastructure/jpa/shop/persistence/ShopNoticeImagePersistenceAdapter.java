package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageSavePort;

import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopNoticeImageJpaEntity.shopNoticeImageJpaEntity;

@Repository
class ShopNoticeImagePersistenceAdapter implements ShopNoticeImageSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopNoticeImageJpaRepository shopNoticeImageJpaRepository;

    public ShopNoticeImagePersistenceAdapter(
        JPAQueryFactory queryFactory,
        ShopNoticeImageJpaRepository shopNoticeImageJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.shopNoticeImageJpaRepository = shopNoticeImageJpaRepository;
    }

    @Override
    public void saveAll(List<ShopNoticeImage> images) {
        List<ShopNoticeImageJpaEntity> entities = images.stream()
            .map(ShopNoticeImageMapper::toEntity)
            .toList();
        shopNoticeImageJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByShopNoticeId(Long shopNoticeId) {
        queryFactory
            .delete(shopNoticeImageJpaEntity)
            .where(shopNoticeImageJpaEntity.shopNoticeId.eq(shopNoticeId))
            .execute();
    }
}
