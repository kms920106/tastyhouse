package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductMapper {

    private ProductMapper() {
    }

    static Product toDomain(ProductJpaEntity entity) {
        return Product.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getProductCategoryId() == null ? null : ProductCategoryId.of(entity.getProductCategoryId()),
            entity.getName(),
            entity.getDescription(),
            entity.getOriginalPrice(),
            toDiscountInfo(entity.getDiscountInfo()),
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
            entity.getVegetarianType() == null ? null : VegetarianType.valueOf(entity.getVegetarianType()),
            entity.getWeightText(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductJpaEntity toEntity(Product product) {
        return ProductJpaEntity.create(
            product.getShopId() == null ? null : product.getShopId().value(),
            product.getProductCategoryId() == null ? null : product.getProductCategoryId().value(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            toEmbeddable(product.getDiscountInfo()),
            product.getRating(),
            product.getReviewCount(),
            product.isRepresentative(),
            product.getSpiciness(),
            product.isSoldOut(),
            product.getSoldOutUntil(),
            product.isVisible(),
            product.getSort(),
            product.isRatingExcluded(),
            product.isDeleted(),
            product.getComposition(),
            product.isSingleServing(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            product.getVegetarianType() == null ? null : product.getVegetarianType().name(),
            product.getWeightText()
        );
    }

    static void applyChanges(ProductJpaEntity entity, Product product) {
        entity.applyChanges(
            product.getProductCategoryId() == null ? null : product.getProductCategoryId().value(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            toEmbeddable(product.getDiscountInfo()),
            product.getRating(),
            product.getReviewCount(),
            product.isRepresentative(),
            product.getSpiciness(),
            product.isSoldOut(),
            product.getSoldOutUntil(),
            product.isVisible(),
            product.getSort(),
            product.isRatingExcluded(),
            product.isDeleted(),
            product.getComposition(),
            product.isSingleServing(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            product.getVegetarianType() == null ? null : product.getVegetarianType().name(),
            product.getWeightText()
        );
    }

    private static ProductDiscountInfo toDiscountInfo(ProductDiscountInfoEmbeddable embeddable) {
        return embeddable == null ? null : ProductDiscountInfo.of(embeddable.discountPrice(), embeddable.discountRate());
    }

    private static ProductDiscountInfoEmbeddable toEmbeddable(ProductDiscountInfo discountInfo) {
        return discountInfo == null
            ? null
            : new ProductDiscountInfoEmbeddable(discountInfo.discountPrice(), discountInfo.discountRate());
    }
}
