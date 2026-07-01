package com.tastyhouse.core.domain.product.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;

import static com.tastyhouse.core.domain.product.domain.model.QProductCategory.productCategory;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JPAQueryFactory queryFactory;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return productCategoryJpaRepository.findById(id);
    }

    @Override
    public List<ProductCategory> findActiveCategoriesByShopIdOrderBySort(Long shopId) {
        return queryFactory
            .selectFrom(productCategory)
            .where(productCategory.shopId.eq(shopId), productCategory.visible.eq(true))
            .orderBy(productCategory.sort.asc())
            .fetch();
    }

    @Override
    public List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId) {
        return queryFactory
            .selectFrom(productCategory)
            .where(productCategory.name.eq(name), productCategory.shopId.eq(shopId))
            .fetch();
    }

    @Override
    public ProductCategory save(ProductCategory entity) {
        return productCategoryJpaRepository.save(entity);
    }
}
