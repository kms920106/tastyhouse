package com.tastyhouse.infrastructure.product.query;

import com.tastyhouse.application.product.port.out.ProductShopLinkQueryPort;
import com.tastyhouse.application.product.port.out.ProductShopLinkResult;
import com.querydsl.core.types.Projections;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
public class ProductShopLinkQueryDao implements ProductShopLinkQueryPort {
    private final JPAQueryFactory queryFactory;

    public ProductShopLinkQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ProductShopLinkResult> findOwnedShopLinks(Long ceoId, Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductShopLinkResult.class,
                shopJpaEntity.id,
                shopJpaEntity.name,
                productShopLinkJpaEntity.productCategoryId,
                productCategoryJpaEntity.name,
                productShopLinkJpaEntity.id.isNotNull()
            ))
            .from(shopJpaEntity)
            .leftJoin(productShopLinkJpaEntity)
            .on(
                productShopLinkJpaEntity.shopId.eq(shopJpaEntity.id),
                productShopLinkJpaEntity.productId.eq(productId)
            )
            .leftJoin(productCategoryJpaEntity)
            .on(productCategoryJpaEntity.id.eq(productShopLinkJpaEntity.productCategoryId))
            .where(shopJpaEntity.ceoId.eq(ceoId))
            .orderBy(shopJpaEntity.id.asc())
            .fetch();
    }

    @Override
    public List<Long> findOwnedShopIds(Long ceoId) {
        return queryFactory
            .select(shopJpaEntity.id)
            .from(shopJpaEntity)
            .where(shopJpaEntity.ceoId.eq(ceoId))
            .fetch();
    }
}
