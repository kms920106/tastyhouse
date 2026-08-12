package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 요청 인덱스 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>ID VO ↔ raw 변환은 nullable 여부를 호출부가 몰라도 안전하도록 {@link IdMapping}으로 통일한다
 * ({@code attachmentFileId}가 실제로 nullable이다).
 */
final class ShopRequestIndexMapper {

    private ShopRequestIndexMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopRequestIndex toDomain(ShopRequestIndexJpaEntity entity) {
        return ShopRequestIndex.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getRequestType(),
            entity.getSourceRequestId(),
            entity.getSummary(),
            entity.getStatus(),
            entity.getRejectReason(),
            IdMapping.vo(entity.getAttachmentFileId(), UploadedFileId::of),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopRequestIndexJpaEntity toEntity(ShopRequestIndex domain) {
        return ShopRequestIndexJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getRequestType(),
            domain.getSourceRequestId(),
            domain.getSummary(),
            domain.getStatus(),
            domain.getRejectReason(),
            IdMapping.raw(domain.getAttachmentFileId(), UploadedFileId::value),
            domain.getRequestedByCeoId(),
            domain.getProcessedAt()
        );
    }

    /**
     * 상태 동기화 결과를 managed 엔티티에 복사한다(load-copy-save).
     */
    static void applyChanges(ShopRequestIndexJpaEntity entity, ShopRequestIndex domain) {
        entity.applyChanges(domain.getStatus(), domain.getRejectReason(), domain.getProcessedAt());
    }
}
