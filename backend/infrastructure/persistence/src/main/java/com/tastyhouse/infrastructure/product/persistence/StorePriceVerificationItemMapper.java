package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 매장 가격 인증 요청 항목 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>항목은 접수 후 변경되지 않으므로 {@code applyChanges}를 두지 않는다(update 경로 없음).
 */
final class StorePriceVerificationItemMapper {

    private StorePriceVerificationItemMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static StorePriceVerificationItem toDomain(StorePriceVerificationItemJpaEntity entity) {
        return StorePriceVerificationItem.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getVerificationId(), StorePriceVerificationId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getProductPriceId(), ProductPriceId::of),
            entity.getStorePrice(),
            entity.isApplyPickupSamePrice(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static StorePriceVerificationItemJpaEntity toEntity(StorePriceVerificationItem domain) {
        return StorePriceVerificationItemJpaEntity.create(
            IdMapping.raw(domain.getVerificationId(), StorePriceVerificationId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getProductPriceId(), ProductPriceId::value),
            domain.getStorePrice(),
            domain.isApplyPickupSamePrice()
        );
    }
}
