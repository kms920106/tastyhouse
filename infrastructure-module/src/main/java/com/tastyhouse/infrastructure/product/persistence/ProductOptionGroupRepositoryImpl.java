package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionGroupId;

import static com.tastyhouse.infrastructure.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductOptionGroupRepositoryImpl implements ProductOptionGroupRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    @Override
    public List<ProductOptionGroup> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productOptionGroupJpaEntity)
            .where(productOptionGroupJpaEntity.productId.eq(productId), productOptionGroupJpaEntity.visible.eq(true))
            .orderBy(productOptionGroupJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductOptionGroupMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
        return productOptionGroupJpaRepository.findById(id.value()).map(ProductOptionGroupMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroup> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionGroupJpaEntity)
            .where(productOptionGroupJpaEntity.id.in(ids))
            .fetch()
            .stream()
            .map(ProductOptionGroupMapper::toDomain)
            .toList();
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup entity) {
        if (entity.getId() == null) {
            ProductOptionGroupJpaEntity saved = productOptionGroupJpaRepository.save(ProductOptionGroupMapper.toEntity(entity));
            return ProductOptionGroupMapper.toDomain(saved);
        }

        ProductOptionGroupJpaEntity jpaEntity = productOptionGroupJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션 그룹입니다: " + entity.getId()));
        ProductOptionGroupMapper.applyChanges(jpaEntity, entity);
        return ProductOptionGroupMapper.toDomain(jpaEntity);
    }
}
