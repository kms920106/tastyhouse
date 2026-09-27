package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopRequestIndexStateMapper {
    private ShopRequestIndexStateMapper() {
    }

    static ShopRequestIndex toDomain(ShopRequestIndexState state) {
        return ShopRequestIndex.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.requestType() == null ? null : ShopRequestType.valueOf(state.requestType()),
            state.sourceRequestId(),
            state.summary(),
            state.status() == null ? null : ShopRequestStatus.valueOf(state.status()),
            state.rejectReason(),
            state.attachmentFileId() == null ? null : UploadedFileId.of(state.attachmentFileId()),
            state.requestedByCeoId(),
            state.processedAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopRequestIndexState toState(ShopRequestIndex shopRequestIndex) {
        return new ShopRequestIndexState(
            shopRequestIndex.getId(),
            shopRequestIndex.getShopId() == null ? null : shopRequestIndex.getShopId().value(),
            shopRequestIndex.getRequestType() == null ? null : shopRequestIndex.getRequestType().name(),
            shopRequestIndex.getSourceRequestId(),
            shopRequestIndex.getSummary(),
            shopRequestIndex.getStatus() == null ? null : shopRequestIndex.getStatus().name(),
            shopRequestIndex.getRejectReason(),
            shopRequestIndex.getAttachmentFileId() == null ? null : shopRequestIndex.getAttachmentFileId().value(),
            shopRequestIndex.getRequestedByCeoId(),
            shopRequestIndex.getProcessedAt(),
            shopRequestIndex.getCreatedAt(),
            shopRequestIndex.getUpdatedAt()
        );
    }
}
