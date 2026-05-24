package com.tastyhouse.core.domain.product.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.domain.product.domain.model.QProductBbq.productBbq;

@Repository
@RequiredArgsConstructor
public class ProductBbqRepositoryImpl implements ProductBbqRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductBbqJpaRepository productBbqJpaRepository;

    @Override
    public Optional<ProductBbq> findByProductId(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbq)
                .where(productBbq.productId.eq(productId))
                .fetchOne()
        );
    }

    @Override
    public Optional<ProductBbq> findFirstWithOptionsSyncPending() {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbq)
                .where(productBbq.isOptionsSynced.eq(false))
                .fetchFirst()
        );
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return queryFactory
            .selectOne()
            .from(productBbq)
            .where(productBbq.productId.eq(productId))
            .fetchFirst() != null;
    }

    @Override
    public ProductBbq save(ProductBbq entity) {
        return productBbqJpaRepository.save(entity);
    }
}
