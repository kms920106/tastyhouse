package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductBbqPersistencePort;

import static com.tastyhouse.infrastructure.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;

@Repository
public class ProductBbqPersistenceAdapter implements ProductBbqPersistencePort {
    private final JPAQueryFactory queryFactory;
    private final ProductBbqJpaRepository productBbqJpaRepository;

    public ProductBbqPersistenceAdapter(JPAQueryFactory queryFactory, ProductBbqJpaRepository productBbqJpaRepository) {
        this.queryFactory = queryFactory;
        this.productBbqJpaRepository = productBbqJpaRepository;
    }

    @Override
    public Optional<ProductBbq> findByProductId(ProductId productId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbqJpaEntity)
                .where(productBbqJpaEntity.productId.eq(productId.value()))
                .fetchOne()
        ).map(ProductBbqMapper::toDomain);
    }

    @Override
    public ProductBbq save(ProductBbq productBbq) {
        if (productBbq.getId() == null) {
            ProductBbqJpaEntity saved = productBbqJpaRepository.save(ProductBbqMapper.toEntity(productBbq));
            return ProductBbqMapper.toDomain(saved);
        }

        ProductBbqJpaEntity jpaEntity = productBbqJpaRepository.findById(productBbq.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 BBQ 매핑입니다: " + productBbq.getId()));
        ProductBbqMapper.applyChanges(jpaEntity, productBbq);
        return ProductBbqMapper.toDomain(jpaEntity);
    }
}
