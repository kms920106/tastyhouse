package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionGroupRepository;

import static com.tastyhouse.infrastructure.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductCommonOptionGroupRepositoryImpl implements ProductCommonOptionGroupRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    @Override
    public List<ProductCommonOptionGroup> findActiveByProductIdOrderBySort(Long productId) {
        return queryFactory
            .selectFrom(productCommonOptionGroupJpaEntity)
            .where(
                productCommonOptionGroupJpaEntity.productId.eq(productId),
                productCommonOptionGroupJpaEntity.visible.eq(true)
            )
            .orderBy(productCommonOptionGroupJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionGroupJpaEntity)
            .where(productCommonOptionGroupJpaEntity.id.in(ids))
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupMapper::toDomain)
            .toList();
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup entity) {
        if (entity.getId() == null) {
            ProductCommonOptionGroupJpaEntity saved =
                productCommonOptionGroupJpaRepository.save(ProductCommonOptionGroupMapper.toEntity(entity));
            return ProductCommonOptionGroupMapper.toDomain(saved);
        }

        ProductCommonOptionGroupJpaEntity jpaEntity = productCommonOptionGroupJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션 그룹입니다: " + entity.getId()));
        ProductCommonOptionGroupMapper.applyChanges(jpaEntity, entity);
        return ProductCommonOptionGroupMapper.toDomain(jpaEntity);
    }
}
