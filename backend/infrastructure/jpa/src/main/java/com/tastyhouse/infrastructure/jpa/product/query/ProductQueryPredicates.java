package com.tastyhouse.infrastructure.jpa.product.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import org.springframework.util.StringUtils;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;

final class ProductQueryPredicates {

    static final com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity subProductImage =
        new com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity("subProductImage");

    private ProductQueryPredicates() {
    }

    static BooleanExpression representativeImageOf(NumberPath<Long> productIdPath) {
        return productImageJpaEntity.productId.eq(productIdPath)
            .and(productImageJpaEntity.visible.eq(true))
            .and(productImageJpaEntity.sort.eq(
                JPAExpressions
                    .select(subProductImage.sort.min())
                    .from(subProductImage)
                    .where(subProductImage.productId.eq(productIdPath)
                        .and(subProductImage.visible.eq(true)))
            ));
    }

    static BooleanExpression notDeleted() {
        return productJpaEntity.deleted.isFalse();
    }

    static BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? productJpaEntity.name.containsIgnoreCase(name) : null;
    }
}
