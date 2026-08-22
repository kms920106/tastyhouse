package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 매장 가격 인증 요청 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환
 * 책임을 infrastructure에 둔다.
 */
final class StorePriceVerificationMapper {

    private StorePriceVerificationMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static StorePriceVerification toDomain(StorePriceVerificationJpaEntity entity) {
        return StorePriceVerification.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getPriceListFileId(), UploadedFileId::of),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static StorePriceVerificationJpaEntity toEntity(StorePriceVerification domain) {
        return StorePriceVerificationJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getPriceListFileId(), UploadedFileId::value),
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getRequestedByCeoId(),
            domain.getProcessedAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(StorePriceVerificationJpaEntity entity, StorePriceVerification domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason(),
            domain.getProcessedAt()
        );
    }
}
