package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductDiscountInfoSnapshot;
import com.tastyhouse.application.product.port.out.write.ProductState;

final class ProductMapper {
    private ProductMapper() {
    }

    static ProductState toState(ProductJpaEntity entity) {
        return new ProductState(
            entity.getId(),
            entity.getShopId(),
            entity.getProductCategoryId(),
            entity.getName(),
            entity.getDescription(),
            entity.getOriginalPrice(),
            toSnapshot(entity.getDiscountInfo()),
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

    static ProductJpaEntity toEntity(ProductState state) {
        return ProductJpaEntity.create(
            state.shopId(),
            state.productCategoryId(),
            state.name(),
            state.description(),
            state.originalPrice(),
            toEmbeddable(state.discountInfo()),
            state.rating(),
            state.reviewCount(),
            state.representative(),
            state.spiciness(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible(),
            state.sort(),
            state.ratingExcluded(),
            state.deleted(),
            state.composition(),
            state.singleServing(),
            state.exposureStartDate(),
            state.exposureEndDate(),
            state.vegetarianType(),
            state.weightText()
        );
    }

    static void applyChanges(ProductJpaEntity entity, ProductState state) {
        entity.applyChanges(
            state.productCategoryId(),
            state.name(),
            state.description(),
            state.originalPrice(),
            toEmbeddable(state.discountInfo()),
            state.rating(),
            state.reviewCount(),
            state.representative(),
            state.spiciness(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible(),
            state.sort(),
            state.ratingExcluded(),
            state.deleted(),
            state.composition(),
            state.singleServing(),
            state.exposureStartDate(),
            state.exposureEndDate(),
            state.vegetarianType(),
            state.weightText()
        );
    }

    private static ProductDiscountInfoSnapshot toSnapshot(ProductDiscountInfoEmbeddable embeddable) {
        return embeddable == null
            ? null
            : new ProductDiscountInfoSnapshot(embeddable.discountPrice(), embeddable.discountRate());
    }

    private static ProductDiscountInfoEmbeddable toEmbeddable(ProductDiscountInfoSnapshot snapshot) {
        return snapshot == null
            ? null
            : new ProductDiscountInfoEmbeddable(snapshot.discountPrice(), snapshot.discountRate());
    }
}
