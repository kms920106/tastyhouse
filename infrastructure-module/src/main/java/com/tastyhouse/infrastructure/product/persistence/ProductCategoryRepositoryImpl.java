package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductCategoryId;

import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    @Override
    public Optional<ProductCategory> findById(ProductCategoryId id) {
        return productCategoryJpaRepository.findById(id.value()).map(ProductCategoryMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findActiveCategoriesByShopIdOrderBySort(Long shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId), productCategoryJpaEntity.visible.eq(true))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.name.eq(name), productCategoryJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ProductCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public ProductCategory save(ProductCategory entity) {
        if (entity.getId() == null) {
            ProductCategoryJpaEntity saved = productCategoryJpaRepository.save(ProductCategoryMapper.toEntity(entity));
            return ProductCategoryMapper.toDomain(saved);
        }

        ProductCategoryJpaEntity jpaEntity = productCategoryJpaRepository.findById(entity.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 카테고리입니다: " + entity.getId()));
        ProductCategoryMapper.applyChanges(jpaEntity, entity);
        return ProductCategoryMapper.toDomain(jpaEntity);
    }
}
