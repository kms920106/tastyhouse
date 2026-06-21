package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
            .where(productCommonOptionGroup.productId.eq(productId), productCommonOptionGroup.isVisible.eq(true))
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
