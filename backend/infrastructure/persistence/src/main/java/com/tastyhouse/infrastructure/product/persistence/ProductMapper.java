package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductMapper {
    private ProductMapper() {
    }

    static Product toDomain(ProductJpaEntity entity) {
        return Product.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getProductCategoryId(), ProductCategoryId::of),
            entity.getName(),
            entity.getDescription(),
            entity.getOriginalPrice(),
            entity.getDiscountInfo(),
            entity.getRating(),
            entity.getReviewCount(),
            entity.isRepresentative(),
            entity.getSpiciness(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible(),
            entity.getSort(),
            entity.isRatingExcluded(),
            entity.isDeleted(),
            entity.getComposition(),
            entity.isSingleServing(),
            entity.getExposureStartDate(),
            entity.getExposureEndDate(),
            entity.getVegetarianType(),
            entity.getWeightText(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductJpaEntity toEntity(Product domain) {
        return ProductJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getProductCategoryId(), ProductCategoryId::value),
            domain.getName(),
            domain.getDescription(),
            domain.getOriginalPrice(),
            domain.getDiscountInfo(),
            domain.getRating(),
            domain.getReviewCount(),
            domain.isRepresentative(),
            domain.getSpiciness(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getSort(),
            domain.isRatingExcluded(),
            domain.isDeleted(),
            domain.getComposition(),
            domain.isSingleServing(),
            domain.getExposureStartDate(),
            domain.getExposureEndDate(),
            domain.getVegetarianType(),
            domain.getWeightText()
        );
    }

    static void applyChanges(ProductJpaEntity entity, Product domain) {
        entity.applyChanges(
            IdMapping.raw(domain.getProductCategoryId(), ProductCategoryId::value),
            domain.getName(),
            domain.getDescription(),
            domain.getOriginalPrice(),
            domain.getDiscountInfo(),
            domain.getRating(),
            domain.getReviewCount(),
            domain.isRepresentative(),
            domain.getSpiciness(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getSort(),
            domain.isRatingExcluded(),
            domain.isDeleted(),
            domain.getComposition(),
            domain.isSingleServing(),
            domain.getExposureStartDate(),
            domain.getExposureEndDate(),
            domain.getVegetarianType(),
            domain.getWeightText()
        );
    }
}
