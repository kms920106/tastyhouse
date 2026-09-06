package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.StationRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopLifecycleService {
    private static final int SHOP_INTRODUCTION_MAX_LENGTH = 500;

    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final StationRepository stationRepository;
    private final ShopImageApprovalService shopImageApprovalService;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopLifecycleService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        StationRepository stationRepository,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopBookmarkRepository = shopBookmarkRepository;
        this.stationRepository = stationRepository;
        this.shopImageApprovalService = shopImageApprovalService;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopCeoAssignmentRecorder = shopCeoAssignmentRecorder;
    }

    public Shop createShop(
        Long adminId,
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        validateStationExists(stationId);
        Shop shop = Shop.of(
            StationId.of(stationId),
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId)
        );
        shop.assignCeo(ceoId == null ? null : CeoId.of(ceoId));
        Shop savedShop = shopRepository.save(shop);

        if (ceoId != null) {
            shopCeoAssignmentRecorder.recordGrant(savedShop.getShopId(), CeoId.of(ceoId), adminId);
        }
        return savedShop;
    }

    public void updateShop(
        ShopId shopId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        validateStationExists(stationId);
        Shop shop = loadShop(shopId);
        shop.update(
            StationId.of(stationId),
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId == null ? null : UploadedFileId.of(thumbnailImageFileId)
        );
        shopRepository.save(shop);
    }

    public void closeShop(ShopId shopId) {
        Shop shop = loadShop(shopId);
        shop.close();
        shopRepository.save(shop);
    }

    public void changeCupDepositEnabled(ShopId shopId, boolean cupDepositEnabled) {
        Shop shop = loadShop(shopId);
        shop.changeCupDepositEnabled(cupDepositEnabled);
        shopRepository.save(shop);
    }

    public void updateHolidayClosure(ShopId shopId, boolean closedOnPublicHolidays, ShopChangeActor actor) {
        Shop shop = loadShop(shopId);
        String previousValue = describeHolidayClosure(shop.isClosedOnPublicHolidays());

        shop.updateHolidayClosure(closedOnPublicHolidays);
        shopRepository.save(shop);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.HOLIDAY_CLOSURE,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeHolidayClosure(shop.isClosedOnPublicHolidays())
        );
    }

    public void changeVisibility(ShopId shopId, boolean hidden, ShopChangeActor actor) {
        if (shopImageApprovalService.existsPendingByShopId(shopId.value())) {
            throw new BusinessException(ErrorCode.SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST);
        }
        Shop shop = loadShop(shopId);
        String previousValue = describeVisibility(shop.isHidden());

        if (hidden) {
            shop.hide();
        } else {
            shop.show();
        }
        shopRepository.save(shop);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.SHOP_VISIBILITY,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeVisibility(shop.isHidden())
        );
    }

    private String describeHolidayClosure(boolean closedOnPublicHolidays) {
        return closedOnPublicHolidays ? "공휴일 휴무" : "공휴일 정상영업";
    }

    private String describeVisibility(boolean hidden) {
        return hidden ? "노출정지" : "노출중";
    }

    public void createOwnerMessage(Long shopId, String message, ShopChangeActor actor) {
        if (message != null && message.length() > SHOP_INTRODUCTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_INTRODUCTION_TOO_LONG);
        }
        prohibitedWordValidator.validate(message);

        String previousValue = describeIntroduction(
            shopDetailRepository.findLatestOwnerMessage(shopId)
                .map(ShopOwnerMessageHistory::getMessage)
                .orElse(null)
        );

        ShopOwnerMessageHistory ownerMessageHistory = ShopOwnerMessageHistory.of(ShopId.of(shopId), message);
        shopDetailRepository.saveOwnerMessage(ownerMessageHistory);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.INTRODUCTION,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeIntroduction(message)
        );
    }

    private String describeIntroduction(String message) {
        return message == null || message.isBlank() ? ShopChangeValueFormatter.unset() : message;
    }

    public boolean toggleBookmark(Long shopId, MemberId memberId) {
        if (shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId)) {
            shopBookmarkRepository.deleteByShopIdAndMemberId(shopId, memberId);
            return false;
        }
        loadShop(ShopId.of(shopId));
        shopBookmarkRepository.save(ShopBookmark.of(ShopId.of(shopId), memberId));
        return true;
    }

    private Shop loadShop(ShopId shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private void validateStationExists(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException(ErrorCode.STATION_NOT_FOUND);
        }
    }
}
