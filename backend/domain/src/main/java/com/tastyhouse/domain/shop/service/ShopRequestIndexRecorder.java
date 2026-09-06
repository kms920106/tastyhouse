package com.tastyhouse.domain.shop.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRequestIndexRecorder {
    private final ShopRequestIndexRepository shopRequestIndexRepository;

    public ShopRequestIndexRecorder(ShopRequestIndexRepository shopRequestIndexRepository) {
        this.shopRequestIndexRepository = shopRequestIndexRepository;
    }

    public void record(
        ShopId shopId,
        ShopRequestType requestType,
        Long sourceRequestId,
        String summary,
        UploadedFileId attachmentFileId,
        Long requestedByCeoId
    ) {
        shopRequestIndexRepository.save(ShopRequestIndex.of(
            shopId,
            requestType,
            sourceRequestId,
            summary,
            attachmentFileId,
            requestedByCeoId
        ));
    }

    public void syncImageChangeStatus(
        ShopRequestType requestType,
        Long sourceRequestId,
        ApprovalStatus status,
        String rejectReason
    ) {
        syncStatus(requestType, sourceRequestId, toRequestStatus(status), rejectReason);
    }

    public void syncAdjustmentStatus(
        Long sourceRequestId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        syncStatus(ShopRequestType.DELIVERY_AREA_ADJUSTMENT, sourceRequestId, toRequestStatus(status), rejectReason);
    }

    public void syncBlindRequestStatus(Long sourceRequestId, ShopRequestStatus status, String rejectReason) {
        syncStatus(ShopRequestType.REVIEW_BLIND, sourceRequestId, status, rejectReason);
    }

    public void syncRequestStatus(
        ShopRequestType requestType,
        Long sourceRequestId,
        ShopRequestStatus status,
        String rejectReason
    ) {
        syncStatus(requestType, sourceRequestId, status, rejectReason);
    }

    public void syncCanceled(ShopRequestType requestType, Long sourceRequestId) {
        syncStatus(requestType, sourceRequestId, ShopRequestStatus.CANCELED, null);
    }

    private void syncStatus(
        ShopRequestType requestType,
        Long sourceRequestId,
        ShopRequestStatus status,
        String rejectReason
    ) {
        ShopRequestIndex index = shopRequestIndexRepository
            .findByRequestTypeAndSourceRequestId(requestType, sourceRequestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
        index.syncStatus(status, rejectReason, LocalDateTime.now());
        shopRequestIndexRepository.save(index);
    }

    private static ShopRequestStatus toRequestStatus(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case APPROVED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    private static ShopRequestStatus toRequestStatus(DeliveryAreaAdjustmentStatus status) {
        return switch (status) {
            case PENDING -> ShopRequestStatus.PENDING;
            case IN_PROGRESS -> ShopRequestStatus.IN_PROGRESS;
            case COMPLETED -> ShopRequestStatus.APPROVED;
            case REJECTED -> ShopRequestStatus.REJECTED;
            case CANCELED -> ShopRequestStatus.CANCELED;
        };
    }

    public ShopRequestIndex getRequest(Long requestId) {
        return shopRequestIndexRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));
    }

    public ShopRequestIndex getRequestOfShop(Long requestId, Long shopId) {
        ShopRequestIndex index = getRequest(requestId);
        if (!index.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND);
        }
        return index;
    }
}
