package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;

@Repository
public class ProductCategoryPersistenceAdapter implements ProductCategoryPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    public ProductCategoryPersistenceAdapter(JPAQueryFactory queryFactory, ProductCategoryJpaRepository productCategoryJpaRepository) {
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
    public ProductCategory save(ProductCategory productCategory) {
        if (productCategory.getId() == null) {
            ProductCategoryJpaEntity saved = productCategoryJpaRepository.save(ProductCategoryMapper.toEntity(productCategory));
            return ProductCategoryMapper.toDomain(saved);
        }

        ProductCategoryJpaEntity jpaEntity = productCategoryJpaRepository.findById(productCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 카테고리입니다: " + productCategory.getId()));
        ProductCategoryMapper.applyChanges(jpaEntity, productCategory);
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
