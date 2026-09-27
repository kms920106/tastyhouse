package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexState;

final class ShopRequestIndexMapper {
    private ShopRequestIndexMapper() {
    }

    static ShopRequestIndexState toState(ShopRequestIndexJpaEntity entity) {
        return new ShopRequestIndexState(
            entity.getId(),
            entity.getShopId(),
            entity.getRequestType(),
            entity.getSourceRequestId(),
            entity.getSummary(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getAttachmentFileId(),
            entity.getRequestedByCeoId(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopRequestIndexJpaEntity toEntity(ShopRequestIndexState state) {
        return ShopRequestIndexJpaEntity.create(
            state.shopId(),
            state.requestType(),
            state.sourceRequestId(),
            state.summary(),
            state.status(),
            state.rejectReason(),
            state.attachmentFileId(),
            state.requestedByCeoId(),
            state.processedAt()
        );
    }

    static void applyChanges(ShopRequestIndexJpaEntity entity, ShopRequestIndexState state) {
        entity.applyChanges(state.status(), state.rejectReason(), state.processedAt());
    }
}
