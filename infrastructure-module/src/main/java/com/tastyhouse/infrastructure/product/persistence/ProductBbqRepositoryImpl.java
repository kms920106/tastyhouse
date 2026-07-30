package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;

import static com.tastyhouse.infrastructure.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;

/**
 * 상품 ↔ BBQ 매핑 write 어댑터. 동기화 대상 탐색은 {@code ProductQueryDao#findFirstBbqSyncTarget}가 담당한다.
 */
@Repository
@RequiredArgsConstructor
public class ProductBbqRepositoryImpl implements ProductBbqRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductBbqJpaRepository productBbqJpaRepository;

    @Override
    public Optional<ProductBbq> findByProductId(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbqJpaEntity)
                .where(productBbqJpaEntity.productId.eq(productId))
                .fetchOne()
        ).map(ProductBbqMapper::toDomain);
    }

    @Override
    public ProductBbq save(ProductBbq entity) {
        if (entity.getId() == null) {
            ProductBbqJpaEntity saved = productBbqJpaRepository.save(ProductBbqMapper.toEntity(entity));
            return ProductBbqMapper.toDomain(saved);
        }

        ProductBbqJpaEntity jpaEntity = productBbqJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 BBQ 매핑입니다: " + entity.getId()));
        ProductBbqMapper.applyChanges(jpaEntity, entity);
        return ProductBbqMapper.toDomain(jpaEntity);
    }
}
