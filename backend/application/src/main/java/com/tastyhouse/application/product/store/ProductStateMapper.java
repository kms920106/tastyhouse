package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductDiscountInfoSnapshot;
import com.tastyhouse.application.product.port.out.write.ProductState;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductStateMapper {
    private ProductStateMapper() {
    }

    static Product toDomain(ProductState state) {
        return Product.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.productCategoryId() == null ? null : ProductCategoryId.of(state.productCategoryId()),
            state.name(),
            state.description(),
            state.originalPrice(),
            toDiscountInfo(state.discountInfo()),
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
            state.vegetarianType() == null ? null : VegetarianType.valueOf(state.vegetarianType()),
            state.weightText(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductState toState(Product product) {
        return new ProductState(
            product.getId(),
            product.getShopId() == null ? null : product.getShopId().value(),
            product.getProductCategoryId() == null ? null : product.getProductCategoryId().value(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            toSnapshot(product.getDiscountInfo()),
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
            product.getWeightText(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    private static ProductDiscountInfo toDiscountInfo(ProductDiscountInfoSnapshot snapshot) {
        return snapshot == null ? null : ProductDiscountInfo.of(snapshot.discountPrice(), snapshot.discountRate());
    }

    private static ProductDiscountInfoSnapshot toSnapshot(ProductDiscountInfo discountInfo) {
        return discountInfo == null
            ? null
            : new ProductDiscountInfoSnapshot(discountInfo.discountPrice(), discountInfo.discountRate());
    }
}
