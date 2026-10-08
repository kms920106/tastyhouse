package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductOptionGroupMergeHistoryJpaEntity.productOptionGroupMergeHistoryJpaEntity;

@Repository
class ProductOptionGroupMergeHistoryPersistenceAdapter implements ProductOptionGroupMergeHistoryPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupMergeHistoryJpaRepository jpaRepository;

    public ProductOptionGroupMergeHistoryPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductOptionGroupMergeHistoryJpaRepository jpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history) {
        ProductOptionGroupMergeHistoryJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeHistoryMapper.toEntity(history));
        return ProductOptionGroupMergeHistoryMapper.toDomain(saved);
    }

    @Override
    public List<ProductOptionGroupMergeHistory> findAllByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(productOptionGroupMergeHistoryJpaEntity)
            .where(productOptionGroupMergeHistoryJpaEntity.shopId.eq(shopId.value()))
            .orderBy(productOptionGroupMergeHistoryJpaEntity.createdAt.desc())
            .fetch()
            .stream()
            .map(ProductOptionGroupMergeHistoryMapper::toDomain)
            .toList();
    }
}
