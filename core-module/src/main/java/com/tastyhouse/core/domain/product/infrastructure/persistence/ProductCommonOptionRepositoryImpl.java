package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.product.domain.model.QProductCommonOption.productCommonOption;

@Repository
@RequiredArgsConstructor
public class ProductCommonOptionRepositoryImpl implements ProductCommonOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    @Override
    public List<ProductCommonOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productCommonOption)
            .where(productCommonOption.optionGroupId.in(optionGroupIds), productCommonOption.isActive.eq(true))
            .orderBy(productCommonOption.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOption> findActiveByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOption)
            .where(productCommonOption.id.in(ids), productCommonOption.isActive.eq(true))
            .fetch();
    }

    @Override
    public ProductCommonOption save(ProductCommonOption entity) {
        return productCommonOptionJpaRepository.save(entity);
    }
}
