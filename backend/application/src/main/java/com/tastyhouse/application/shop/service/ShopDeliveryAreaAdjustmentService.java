package com.tastyhouse.application.shop.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaAdjustmentRequest;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestPersistencePort;

public class ShopDeliveryAreaAdjustmentService {
    private static final List<DeliveryAreaAdjustmentStatus> OPEN_STATUSES =
        List.of(DeliveryAreaAdjustmentStatus.PENDING, DeliveryAreaAdjustmentStatus.IN_PROGRESS);

    private final ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopDeliveryAreaAdjustmentService(
        ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopDeliveryAreaAdjustmentRequestPersistencePort = shopDeliveryAreaAdjustmentRequestPersistencePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    public Long request(
        ShopId shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        UploadedFileId consentFileId,
        ShopChangeActor actor
    ) {
        if (shopDeliveryAreaAdjustmentRequestPersistencePort.existsByShopIdAndStatusIn(shopId, OPEN_STATUSES)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_ALREADY_PENDING);
        }

        ShopDeliveryAreaAdjustmentRequest saved = shopDeliveryAreaAdjustmentRequestPersistencePort.save(
            ShopDeliveryAreaAdjustmentRequest.of(
                shopId,
                counterpartShopName,
                counterpartBusinessNumber,
                franchiseName,
                reason,
                consentFileId
            )
        );

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_ADJUSTMENT,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeAdjustmentRequest(saved)
        );

        shopRequestIndexRecorder.record(
            shopId,
            ShopRequestType.DELIVERY_AREA_ADJUSTMENT,
            saved.getId(),
            describeAdjustmentRequest(saved),
            saved.getConsentFileId(),
            actor.actorId()
        );
        return saved.getId();
    }

    private String describeAdjustmentRequest(ShopDeliveryAreaAdjustmentRequest request) {
        return request.getCounterpartShopName() + " (" + request.getFranchiseName() + ")";
    }

    public void startProgress(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.startProgress();
        shopDeliveryAreaAdjustmentRequestPersistencePort.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), null);
    }

    public void complete(Long requestId) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.complete();
        shopDeliveryAreaAdjustmentRequestPersistencePort.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), null);
    }

    public void reject(Long requestId, String reason) {
        ShopDeliveryAreaAdjustmentRequest request = findRequest(requestId);
        request.reject(reason);
        shopDeliveryAreaAdjustmentRequestPersistencePort.save(request);
        shopRequestIndexRecorder.syncAdjustmentStatus(requestId, request.getStatus(), reason);
    }

    private ShopDeliveryAreaAdjustmentRequest findRequest(Long requestId) {
        return shopDeliveryAreaAdjustmentRequestPersistencePort.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST_NOT_FOUND));
    }
}
