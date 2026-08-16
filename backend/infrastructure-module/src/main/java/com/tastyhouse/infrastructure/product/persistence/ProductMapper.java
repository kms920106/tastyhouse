package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 상품 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductMapper {

    private ProductMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
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
            entity.isVisible(),
            entity.getSort(),
            entity.isRatingExcluded(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
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
            domain.isVisible(),
            domain.getSort(),
            domain.isRatingExcluded()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     *
     * <p>{@code ratingExcluded}는 <b>의도적으로 복사하지 않는다</b> — 도메인 모델에서 {@code final}이라
     * 상품 수정 경로가 이 값을 바꾸지 않는다.
     */
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
            domain.isVisible(),
            domain.getSort()
        );
    }
}
