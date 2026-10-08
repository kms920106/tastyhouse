package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;

@Repository
class ProductOptionPersistenceAdapter implements ProductOptionPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionJpaRepository productOptionJpaRepository;

    public ProductOptionPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductOptionJpaRepository productOptionJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productOptionJpaRepository = productOptionJpaRepository;
    }

    @Override
    public Optional<ProductOption> findById(ProductOptionId id) {
        return productOptionJpaRepository.findById(id.value()).map(ProductOptionMapper::toDomain);
    }

    @Override
    public ProductOption save(ProductOption productOption) {
        if (productOption.getId() == null) {
            ProductOptionJpaEntity saved = productOptionJpaRepository.save(ProductOptionMapper.toEntity(productOption));
            return ProductOptionMapper.toDomain(saved);
        }

        ProductOptionJpaEntity jpaEntity = productOptionJpaRepository.findById(productOption.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션입니다: " + productOption.getId()));
        ProductOptionMapper.applyChanges(jpaEntity, productOption);
        return ProductOptionMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductOption> findAllByIdIn(List<ProductOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionJpaEntity)
            .where(productOptionJpaEntity.id.in(ids.stream().map(ProductOptionId::value).toList()))
            .fetch()
            .stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectFrom(productOptionJpaEntity)
            .where(productOptionJpaEntity.optionGroupId.eq(optionGroupId.value()))
            .fetch()
            .stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return queryFactory
            .selectFrom(productOptionJpaEntity)
            .where(
                productOptionJpaEntity.soldOut.isTrue(),
                productOptionJpaEntity.soldOutUntil.isNotNull(),
                productOptionJpaEntity.soldOutUntil.loe(baseTime)
            )
            .fetch()
            .stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }
}
