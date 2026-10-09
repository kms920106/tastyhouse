package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOption;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;

@Repository
class ProductCommonOptionPersistenceAdapter implements ProductCommonOptionLoadPort, ProductCommonOptionSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    public ProductCommonOptionPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductCommonOptionJpaRepository productCommonOptionJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productCommonOptionJpaRepository = productCommonOptionJpaRepository;
    }

    @Override
    public Optional<ProductCommonOption> findById(ProductCommonOptionId id) {
        return productCommonOptionJpaRepository.findById(id.value()).map(ProductCommonOptionMapper::toDomain);
    }

    @Override
    public ProductCommonOption save(ProductCommonOption productCommonOption) {
        if (productCommonOption.getId() == null) {
            ProductCommonOptionJpaEntity saved =
                productCommonOptionJpaRepository.save(ProductCommonOptionMapper.toEntity(productCommonOption));
            return ProductCommonOptionMapper.toDomain(saved);
        }

        ProductCommonOptionJpaEntity jpaEntity = productCommonOptionJpaRepository.findById(productCommonOption.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션입니다: " + productCommonOption.getId()));
        ProductCommonOptionMapper.applyChanges(jpaEntity, productCommonOption);
        return ProductCommonOptionMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOption> findAllByIdIn(List<ProductCommonOptionId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionJpaEntity)
            .where(productCommonOptionJpaEntity.id.in(ids.stream().map(ProductCommonOptionId::value).toList()))
            .fetch()
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectFrom(productCommonOptionJpaEntity)
            .where(productCommonOptionJpaEntity.optionGroupId.eq(optionGroupId.value()))
            .fetch()
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return queryFactory
            .selectFrom(productCommonOptionJpaEntity)
            .where(
                productCommonOptionJpaEntity.soldOut.isTrue(),
                productCommonOptionJpaEntity.soldOutUntil.isNotNull(),
                productCommonOptionJpaEntity.soldOutUntil.loe(baseTime)
            )
            .fetch()
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }
}
