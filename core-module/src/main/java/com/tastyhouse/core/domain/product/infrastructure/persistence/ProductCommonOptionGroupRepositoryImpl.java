package com.tastyhouse.core.domain.product.infrastructure.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionGroupRepository;

import static com.tastyhouse.core.domain.product.domain.model.QProductCommonOptionGroup.productCommonOptionGroup;

@Repository
@RequiredArgsConstructor
public class ProductCommonOptionGroupRepositoryImpl implements ProductCommonOptionGroupRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    @Override
    public List<ProductCommonOptionGroup> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productCommonOptionGroup)
            .where(productCommonOptionGroup.productId.eq(productId), productCommonOptionGroup.visible.eq(true))
            .orderBy(productCommonOptionGroup.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionGroup)
            .where(productCommonOptionGroup.id.in(ids))
            .fetch();
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup entity) {
        return productCommonOptionGroupJpaRepository.save(entity);
    }
}
