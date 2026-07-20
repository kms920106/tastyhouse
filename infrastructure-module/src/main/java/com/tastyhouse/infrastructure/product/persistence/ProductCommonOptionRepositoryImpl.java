package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionRepository;

import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionJpaEntity.productCommonOptionJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductCommonOptionRepositoryImpl implements ProductCommonOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    @Override
    public List<ProductCommonOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productCommonOptionJpaEntity)
            .where(
                productCommonOptionJpaEntity.optionGroupId.in(optionGroupIds),
                productCommonOptionJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOption> findActiveByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionJpaEntity)
            .where(productCommonOptionJpaEntity.id.in(ids), productCommonOptionJpaEntity.visible.eq(true))
            .fetch()
            .stream()
            .map(ProductCommonOptionMapper::toDomain)
            .toList();
    }

    @Override
    public ProductCommonOption save(ProductCommonOption entity) {
        if (entity.getId() == null) {
            ProductCommonOptionJpaEntity saved =
                productCommonOptionJpaRepository.save(ProductCommonOptionMapper.toEntity(entity));
            return ProductCommonOptionMapper.toDomain(saved);
        }

        ProductCommonOptionJpaEntity jpaEntity = productCommonOptionJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션입니다: " + entity.getId()));
        ProductCommonOptionMapper.applyChanges(jpaEntity, entity);
        return ProductCommonOptionMapper.toDomain(jpaEntity);
    }
}
