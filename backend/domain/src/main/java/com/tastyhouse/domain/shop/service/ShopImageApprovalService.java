package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ShopImageApprovalService {
    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopImageApprovalService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopImageChangeRequestRepository = shopImageChangeRequestRepository;
        this.shopRepository = shopRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    public Long requestImageChange(Long shopId, ShopImageType imageType, Long imageFileId, ShopChangeActor actor) {
        if (shopImageChangeRequestRepository.existsByShopIdAndImageTypeAndStatus(shopId, imageType, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ShopImageChangeRequest saved = shopImageChangeRequestRepository.save(
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
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.approve();
        shopImageChangeRequestRepository.save(shopImageChangeRequest);

        ShopId shopId = shopImageChangeRequest.getShopId();
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shopImageChangeRequest.getImageType() == ShopImageType.TRADEMARK) {
            shop.changeTrademarkImage(shopImageChangeRequest.getImageFileId());
        } else {
            shop.changeThumbnailImage(shopImageChangeRequest.getImageFileId());
        }
        shopRepository.save(shop);

        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            null
        );
    }

    public void rejectImageChange(Long id, String reason) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.reject(reason);
        shopImageChangeRequestRepository.save(shopImageChangeRequest);
        shopRequestIndexRecorder.syncImageChangeStatus(
            requestTypeOf(shopImageChangeRequest.getImageType()),
            id,
            shopImageChangeRequest.getStatus(),
            reason
        );
    }

    public boolean existsPendingByShopId(Long shopId) {
        return shopImageChangeRequestRepository.existsByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
    }
}
