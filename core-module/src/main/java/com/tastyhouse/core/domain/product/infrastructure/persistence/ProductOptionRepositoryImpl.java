package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.product.domain.model.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionJpaRepository productOptionJpaRepository;

    @Override
    public List<ProductOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productOption)
            .where(productOption.optionGroupId.in(optionGroupIds), productOption.isVisible.eq(true))
            .orderBy(productOption.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ProductOption> findById(Long id) {
        return productOptionJpaRepository.findById(id);
    }

    @Override
    public List<ProductOption> findActiveByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOption)
            .where(productOption.id.in(ids), productOption.isVisible.eq(true))
            .fetch();
    }

    @Override
    public ProductOption save(ProductOption entity) {
        return productOptionJpaRepository.save(entity);
    }
}
