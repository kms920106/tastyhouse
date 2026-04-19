package com.tastyhouse.core.repository.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.product.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.product.QProductCategory.productCategory;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductCategory> findByNameAndPlaceId(String name, Long placeId) {
        return queryFactory
            .selectFrom(productCategory)
            .where(
                productCategory.name.eq(name),
                productCategory.placeId.eq(placeId)
            )
            .fetch();
    }
}
