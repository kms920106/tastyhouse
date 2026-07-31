package com.tastyhouse.infrastructure.shop.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shop.domain.model.ShopBookmark;
import com.tastyhouse.domain.shop.domain.repository.ShopBookmarkRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QShopBookmarkJpaEntity.shopBookmarkJpaEntity;

@Repository
@RequiredArgsConstructor
public class ShopBookmarkRepositoryImpl implements ShopBookmarkRepository {

    private final JPAQueryFactory queryFactory;
    private final ShopBookmarkJpaRepository shopBookmarkJpaRepository;

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
        queryFactory
            .delete(shopBookmarkJpaEntity)
            .where(shopBookmarkJpaEntity.shopId.eq(shopId), shopBookmarkJpaEntity.memberId.eq(memberId))
            .execute();
    }

    @Override
    public ShopBookmark save(ShopBookmark shopBookmark) {
        if (shopBookmark.getId() == null) {
            ShopBookmarkJpaEntity saved = shopBookmarkJpaRepository.save(ShopBookmarkMapper.toEntity(shopBookmark));
            return ShopBookmarkMapper.toDomain(saved);
        }

        // update 경로 없음(ShopBookmark는 insert-only) — 존재 시에도 재조회만 수행
        ShopBookmarkJpaEntity entity = shopBookmarkJpaRepository.findById(shopBookmark.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 북마크입니다: " + shopBookmark.getId()));
        return ShopBookmarkMapper.toDomain(entity);
    }
}
