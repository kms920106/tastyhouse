package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;

@Repository
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {
    private final JPAQueryFactory queryFactory;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    public ProductCategoryRepositoryImpl(JPAQueryFactory queryFactory, ProductCategoryJpaRepository productCategoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.productCategoryJpaRepository = productCategoryJpaRepository;
    }

    @Override
    public Optional<ProductCategory> findById(ProductCategoryId id) {
        return productCategoryJpaRepository.findById(id.value()).map(ProductCategoryMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.name.eq(name), productCategoryJpaEntity.shopId.eq(shopId.value()))
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

    @Override
    public List<ProductCategory> findAllByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId.value()))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ProductCategory productCategory) {
        productCategoryJpaRepository.deleteById(productCategory.getId());
    }
}
