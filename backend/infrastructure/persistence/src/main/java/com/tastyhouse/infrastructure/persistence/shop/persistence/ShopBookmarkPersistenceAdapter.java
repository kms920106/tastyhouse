package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkSavePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;

@Repository
class ShopBookmarkPersistenceAdapter implements ShopBookmarkLoadPort, ShopBookmarkSavePort {

    private final JPAQueryFactory queryFactory;
    private final ShopBookmarkJpaRepository shopBookmarkJpaRepository;

    public ShopBookmarkPersistenceAdapter(JPAQueryFactory queryFactory, ShopBookmarkJpaRepository shopBookmarkJpaRepository) {
        this.queryFactory = queryFactory;
        this.shopBookmarkJpaRepository = shopBookmarkJpaRepository;
    }

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId.value()))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
        queryFactory
            .delete(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId.value()))
            .execute();
    }

    @Override
    public ShopBookmark save(ShopBookmark shopBookmark) {
        if (shopBookmark.getId() == null) {
            ShopBookmarkJpaEntity saved = shopBookmarkJpaRepository.save(ShopBookmarkMapper.toEntity(shopBookmark));
            return ShopBookmarkMapper.toDomain(saved);
        }

        ShopBookmarkJpaEntity entity = shopBookmarkJpaRepository.findById(shopBookmark.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 북마크입니다: " + shopBookmark.getId()));
        return ShopBookmarkMapper.toDomain(entity);
    }
}
