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

    /**
     * QueryDSL bulk delete라 1차 캐시를 우회한다 — 같은 트랜잭션에서 삭제 대상 이미지를 다시 읽으면
     * 영속성 컨텍스트에 남은 엔티티가 돌아온다. replace-all 경로는 삭제 후 새 엔티티만 insert하므로
     * 안전하지만, 재조회를 추가할 때는 이 점을 확인해야 한다.
     */
    @Override
    public void deleteByShopNoticeId(Long shopNoticeId) {
        queryFactory
            .delete(shopNoticeImageJpaEntity)
            .where(shopNoticeImageJpaEntity.shopNoticeId.eq(shopNoticeId))
            .execute();
    }
}
