package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.domain.shop.domain.model.QShopBookmark.shopBookmark;

@Repository
@RequiredArgsConstructor
public class ShopBookmarkRepositoryImpl implements ShopBookmarkRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ShopBookmark> findByShopIdAndMemberId(Long shopId, Long memberId) {
        ShopBookmark result = queryFactory
            .selectFrom(shopBookmark)
            .where(shopBookmark.shopId.eq(shopId), shopBookmark.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(shopBookmark)
            .where(shopBookmark.shopId.eq(shopId), shopBookmark.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, Long memberId) {
        queryFactory
            .delete(shopBookmark)
            .where(shopBookmark.shopId.eq(shopId), shopBookmark.memberId.eq(memberId))
            .execute();
    }
}
