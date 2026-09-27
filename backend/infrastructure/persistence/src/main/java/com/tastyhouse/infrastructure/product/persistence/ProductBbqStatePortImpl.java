package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductBbqState;
import com.tastyhouse.application.product.port.out.write.ProductBbqStatePort;

import static com.tastyhouse.infrastructure.product.persistence.QProductBbqJpaEntity.productBbqJpaEntity;

@Repository
public class ProductBbqStatePortImpl implements ProductBbqStatePort {
    private final JPAQueryFactory queryFactory;
    private final ProductBbqJpaRepository productBbqJpaRepository;

    public ProductBbqStatePortImpl(JPAQueryFactory queryFactory, ProductBbqJpaRepository productBbqJpaRepository) {
        this.queryFactory = queryFactory;
        this.productBbqJpaRepository = productBbqJpaRepository;
    }

    @Override
    public Optional<ProductBbqState> findByProductId(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(productBbqJpaEntity)
                .where(productBbqJpaEntity.productId.eq(productId))
                .fetchOne()
        ).map(ProductBbqMapper::toState);
    }

    @Override
    public ProductBbqState save(ProductBbqState state) {
        if (state.id() == null) {
            ProductBbqJpaEntity saved = productBbqJpaRepository.save(ProductBbqMapper.toEntity(state));
            return ProductBbqMapper.toState(saved);
        }

        ProductBbqJpaEntity jpaEntity = productBbqJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 BBQ 매핑입니다: " + state.id()));
        ProductBbqMapper.applyChanges(jpaEntity, state);
        return ProductBbqMapper.toState(jpaEntity);
    }
}
