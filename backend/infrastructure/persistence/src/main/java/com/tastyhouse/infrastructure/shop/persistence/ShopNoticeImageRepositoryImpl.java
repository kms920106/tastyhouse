package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.domain.shop.repository.ShopNoticeImageRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QShopNoticeImageJpaEntity.shopNoticeImageJpaEntity;

@Repository
public class ShopNoticeImageRepositoryImpl implements ShopNoticeImageRepository {
    private final JPAQueryFactory queryFactory;
    private final ShopNoticeImageJpaRepository shopNoticeImageJpaRepository;

    public ShopNoticeImageRepositoryImpl(
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
