package com.tastyhouse.core.repository.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.QProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductCategory> findByNameAndPlaceId(String name, Long placeId) {
        QProductCategory productCategory = QProductCategory.productCategory;

        return queryFactory
            .selectFrom(productCategory)
            .where(
                productCategory.name.eq(name),
                productCategory.placeId.eq(placeId)
            )
            .fetch();
    }
}
