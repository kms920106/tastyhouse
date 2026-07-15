package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;

import static com.tastyhouse.core.domain.shop.domain.model.QShopBookmark.shopBookmark;

@Repository
@RequiredArgsConstructor
public class ShopBookmarkRepositoryImpl implements ShopBookmarkRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(shopBookmark)
            .where(shopBookmark.shopId.eq(shopId), shopBookmark.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
        queryFactory
            .delete(shopBookmark)
            .where(shopBookmark.shopId.eq(shopId), shopBookmark.memberId.eq(memberId))
            .execute();
    }
}
