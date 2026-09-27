package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryAreaAdjustmentRequestStateMapper {
    private ShopDeliveryAreaAdjustmentRequestStateMapper() {
    }

    static ShopDeliveryAreaAdjustmentRequest toDomain(ShopDeliveryAreaAdjustmentRequestState state) {
        return ShopDeliveryAreaAdjustmentRequest.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.counterpartShopName(),
            state.counterpartBusinessNumber(),
            state.franchiseName(),
            state.reason(),
            state.consentFileId() == null ? null : UploadedFileId.of(state.consentFileId()),
            state.status() == null ? null : DeliveryAreaAdjustmentStatus.valueOf(state.status()),
            state.rejectReason(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopDeliveryAreaAdjustmentRequestState toState(ShopDeliveryAreaAdjustmentRequest shopDeliveryAreaAdjustmentRequest) {
        return new ShopDeliveryAreaAdjustmentRequestState(
            shopDeliveryAreaAdjustmentRequest.getId(),
            shopDeliveryAreaAdjustmentRequest.getShopId() == null ? null : shopDeliveryAreaAdjustmentRequest.getShopId().value(),
            shopDeliveryAreaAdjustmentRequest.getCounterpartShopName(),
            shopDeliveryAreaAdjustmentRequest.getCounterpartBusinessNumber(),
            shopDeliveryAreaAdjustmentRequest.getFranchiseName(),
            shopDeliveryAreaAdjustmentRequest.getReason(),
            shopDeliveryAreaAdjustmentRequest.getConsentFileId() == null ? null : shopDeliveryAreaAdjustmentRequest.getConsentFileId().value(),
            shopDeliveryAreaAdjustmentRequest.getStatus() == null ? null : shopDeliveryAreaAdjustmentRequest.getStatus().name(),
            shopDeliveryAreaAdjustmentRequest.getRejectReason(),
            shopDeliveryAreaAdjustmentRequest.getCreatedAt(),
            shopDeliveryAreaAdjustmentRequest.getUpdatedAt()
        );
    }
}
