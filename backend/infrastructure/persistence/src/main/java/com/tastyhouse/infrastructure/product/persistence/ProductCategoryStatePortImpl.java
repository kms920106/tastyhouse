package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductCategoryState;
import com.tastyhouse.application.product.port.out.write.ProductCategoryStatePort;

import static com.tastyhouse.infrastructure.product.persistence.QProductCategoryJpaEntity.productCategoryJpaEntity;

@Repository
public class ProductCategoryStatePortImpl implements ProductCategoryStatePort {
    private final JPAQueryFactory queryFactory;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    public ProductCategoryStatePortImpl(JPAQueryFactory queryFactory, ProductCategoryJpaRepository productCategoryJpaRepository) {
        this.queryFactory = queryFactory;
        this.productCategoryJpaRepository = productCategoryJpaRepository;
    }

    @Override
    public Optional<ProductCategoryState> findById(Long id) {
        return productCategoryJpaRepository.findById(id).map(ProductCategoryMapper::toState);
    }

    @Override
    public List<ProductCategoryState> findCategoriesByNameAndShopId(String name, Long shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.name.eq(name), productCategoryJpaEntity.shopId.eq(shopId))
            .fetch()
            .stream()
            .map(ProductCategoryMapper::toState)
            .toList();
    }

    @Override
    public ProductCategoryState save(ProductCategoryState state) {
        if (state.id() == null) {
            ProductCategoryJpaEntity saved = productCategoryJpaRepository.save(ProductCategoryMapper.toEntity(state));
            return ProductCategoryMapper.toState(saved);
        }

        ProductCategoryJpaEntity jpaEntity = productCategoryJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 카테고리입니다: " + state.id()));
        ProductCategoryMapper.applyChanges(jpaEntity, state);
        return ProductCategoryMapper.toState(jpaEntity);
    }

    @Override
    public List<ProductCategoryState> findAllByShopId(Long shopId) {
        return queryFactory
            .selectFrom(productCategoryJpaEntity)
            .where(productCategoryJpaEntity.shopId.eq(shopId))
            .orderBy(productCategoryJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCategoryMapper::toState)
            .toList();
    }

    @Override
    public void deleteById(Long id) {
        productCategoryJpaRepository.deleteById(id);
    }
}
