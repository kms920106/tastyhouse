package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;

@Service
public class ShopImageApprovalService {

    private final ShopImageChangeRequestLoadPort shopImageChangeRequestLoadPort;
    private final ShopImageChangeRequestSavePort shopImageChangeRequestSavePort;
    private final ShopLoadPort shopLoadPort;
    private final ShopSavePort shopSavePort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopImageApprovalService(
        ShopImageChangeRequestLoadPort shopImageChangeRequestLoadPort,
        ShopImageChangeRequestSavePort shopImageChangeRequestSavePort,
        ShopLoadPort shopLoadPort,
        ShopSavePort shopSavePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestLoadPort = shopImageChangeRequestLoadPort;
        this.shopImageChangeRequestSavePort = shopImageChangeRequestSavePort;
        this.shopLoadPort = shopLoadPort;
        this.shopSavePort = shopSavePort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    public Long requestImageChange(Long shopId, ShopImageType imageType, Long imageFileId, ShopChangeActor actor) {
        if (shopImageChangeRequestLoadPort.existsByShopIdAndImageTypeAndStatus(shopId, imageType, ApprovalStatus.PENDING)) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ShopImageChangeRequest saved = shopImageChangeRequestSavePort.save(
            ShopImageChangeRequest.of(ShopId.of(shopId), imageType, UploadedFileId.of(imageFileId))
        );

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            changeTypeOf(imageType),
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeImageChangeRequest(imageType, imageFileId)
        );

        shopRequestIndexRecorder.record(
            ShopId.of(shopId),
            requestTypeOf(imageType),
            saved.getId(),
            describeImageChangeRequest(imageType, imageFileId),
            saved.getImageFileId(),
            actor.actorId()
        );
        return saved.getId();
    }

    private ShopRequestType requestTypeOf(ShopImageType imageType) {
        return imageType == ShopImageType.TRADEMARK
            ? ShopRequestType.TRADEMARK_CHANGE
            : ShopRequestType.THUMBNAIL_CHANGE;
    }

    private ShopChangeType changeTypeOf(ShopImageType imageType) {
        return imageType == ShopImageType.TRADEMARK
            ? ShopChangeType.TRADEMARK_CHANGE_REQUEST
            : ShopChangeType.THUMBNAIL_CHANGE_REQUEST;
    }

    private String describeImageChangeRequest(ShopImageType imageType, Long imageFileId) {
        return changeTypeOf(imageType).getDescription() + "(파일 #" + imageFileId + ")";
    }

    public void approveImageChange(Long id) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestLoadPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.approve();
        shopImageChangeRequestSavePort.save(shopImageChangeRequest);

        ShopId shopId = shopImageChangeRequest.getShopId();
        Shop shop = shopLoadPort.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
        if (shopImageChangeRequest.getImageType() == ShopImageType.TRADEMARK) {
            shop.changeTrademarkImage(shopImageChangeRequest.getImageFileId());
        } else {
            shop.changeThumbnailImage(shopImageChangeRequest.getImageFileId());
        }
        shopSavePort.save(shop);

        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            null
        );
    }

    public void rejectImageChange(Long id, String reason) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestLoadPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.reject(reason);
        shopImageChangeRequestSavePort.save(shopImageChangeRequest);
        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            reason
        );
    }

    public boolean existsPendingByShopId(Long shopId) {
        return shopImageChangeRequestLoadPort.existsByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
    }
}
