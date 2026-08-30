package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 주문 상품 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class OrderProductMapper {

    private OrderProductMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static OrderProduct toDomain(OrderProductJpaEntity entity) {
        return OrderProduct.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOrderId(), OrderId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getName(),
            entity.getPriceName(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getQuantity(),
            entity.getOriginalPrice(),
            entity.getDiscountPrice(),
            entity.getTotalOptionPrice(),
            entity.getTotalPrice(),
            entity.getCupDepositAmount()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static OrderProductJpaEntity toEntity(OrderProduct domain) {
        return OrderProductJpaEntity.create(
            IdMapping.raw(domain.getOrderId(), OrderId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getName(),
            domain.getPriceName(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getQuantity(),
            domain.getOriginalPrice(),
            domain.getDiscountPrice(),
            domain.getTotalOptionPrice(),
            domain.getTotalPrice(),
            domain.getCupDepositAmount()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(OrderProductJpaEntity entity, OrderProduct domain) {
        entity.applyChanges(
            domain.getTotalOptionPrice(),
            domain.getTotalPrice(),
            domain.getCupDepositAmount()
        );
    }
}
