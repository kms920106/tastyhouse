package com.tastyhouse.infrastructure.shop.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopBookmarkState;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkStatePort;

import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;

@Repository
public class ShopBookmarkStatePortImpl implements ShopBookmarkStatePort {
    private final JPAQueryFactory queryFactory;
    private final ShopBookmarkJpaRepository shopBookmarkJpaRepository;

    public ShopBookmarkStatePortImpl(JPAQueryFactory queryFactory, ShopBookmarkJpaRepository shopBookmarkJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopBookmarkJpaRepository = shopBookmarkJpaRepository;
    }

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, Long memberId) {
        queryFactory
            .delete(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId))
            .execute();
    }

    @Override
    public ShopBookmarkState save(ShopBookmarkState shopBookmark) {
        if (shopBookmark.id() == null) {
            ShopBookmarkJpaEntity saved = shopBookmarkJpaRepository.save(ShopBookmarkMapper.toEntity(shopBookmark));
            return ShopBookmarkMapper.toState(saved);
        }

        ShopBookmarkJpaEntity entity = shopBookmarkJpaRepository.findById(shopBookmark.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 북마크입니다: " + shopBookmark.id()));
        return ShopBookmarkMapper.toState(entity);
    }
}
