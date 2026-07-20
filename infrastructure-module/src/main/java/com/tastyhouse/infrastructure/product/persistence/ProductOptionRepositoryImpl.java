package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;

import static com.tastyhouse.infrastructure.product.persistence.QProductOptionJpaEntity.productOptionJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionJpaRepository productOptionJpaRepository;

    @Override
    public List<ProductOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds) {
        return queryFactory
            .selectFrom(productOptionJpaEntity)
            .where(productOptionJpaEntity.optionGroupId.in(optionGroupIds), productOptionJpaEntity.visible.eq(true))
            .orderBy(productOptionJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ProductOption> findById(ProductOptionId id) {
        return productOptionJpaRepository.findById(id.value()).map(ProductOptionMapper::toDomain);
    }

    @Override
    public List<ProductOption> findActiveByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionJpaEntity)
            .where(productOptionJpaEntity.id.in(ids), productOptionJpaEntity.visible.eq(true))
            .fetch()
            .stream()
            .map(ProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public ProductOption save(ProductOption entity) {
        if (entity.getId() == null) {
            ProductOptionJpaEntity saved = productOptionJpaRepository.save(ProductOptionMapper.toEntity(entity));
            return ProductOptionMapper.toDomain(saved);
        }

        ProductOptionJpaEntity jpaEntity = productOptionJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션입니다: " + entity.getId()));
        ProductOptionMapper.applyChanges(jpaEntity, entity);
        return ProductOptionMapper.toDomain(jpaEntity);
    }
}
