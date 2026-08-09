package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 배달지역 조정 신청 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을
 * infrastructure에 둔다.
 */
final class ShopDeliveryAreaAdjustmentRequestMapper {

    private ShopDeliveryAreaAdjustmentRequestMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopDeliveryAreaAdjustmentRequest toDomain(ShopDeliveryAreaAdjustmentRequestJpaEntity entity) {
        return ShopDeliveryAreaAdjustmentRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getCounterpartShopName(),
            entity.getCounterpartBusinessNumber(),
            entity.getFranchiseName(),
            entity.getReason(),
            IdMapping.vo(entity.getConsentFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopDeliveryAreaAdjustmentRequestJpaEntity toEntity(ShopDeliveryAreaAdjustmentRequest domain) {
        return ShopDeliveryAreaAdjustmentRequestJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getCounterpartShopName(),
            domain.getCounterpartBusinessNumber(),
            domain.getFranchiseName(),
            domain.getReason(),
            IdMapping.raw(domain.getConsentFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopDeliveryAreaAdjustmentRequestJpaEntity entity, ShopDeliveryAreaAdjustmentRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
