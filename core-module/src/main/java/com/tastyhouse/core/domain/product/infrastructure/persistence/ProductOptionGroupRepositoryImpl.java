package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.product.domain.model.QProductOptionGroup.productOptionGroup;

@Repository
@RequiredArgsConstructor
public class ProductOptionGroupRepositoryImpl implements ProductOptionGroupRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    @Override
    public List<ProductOptionGroup> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productOptionGroup)
            .where(productOptionGroup.productId.eq(productId), productOptionGroup.isVisible.eq(true))
            .orderBy(productOptionGroup.sort.asc())
            .fetch();
    }

    @Override
    public Optional<ProductOptionGroup> findById(Long id) {
        return productOptionGroupJpaRepository.findById(id);
    }

    @Override
    public List<ProductOptionGroup> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionGroup)
            .where(productOptionGroup.id.in(ids))
            .fetch();
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return queryFactory
            .selectOne()
            .from(productOptionGroup)
            .where(productOptionGroup.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup entity) {
        return productOptionGroupJpaRepository.save(entity);
    }
}
