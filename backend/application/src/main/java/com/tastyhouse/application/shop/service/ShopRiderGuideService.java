package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuidePersistencePort;

@SharedApp
public class ShopRiderGuideService {

    private final ShopRiderGuidePersistencePort shopRiderGuidePersistencePort;
    private final ShopPersistencePort shopPersistencePort;
    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopRiderGuideService(
        ShopRiderGuidePersistencePort shopRiderGuidePersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRiderGuidePersistencePort = shopRiderGuidePersistencePort;
        this.shopPersistencePort = shopPersistencePort;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public void updateVisitGuide(Long shopId, String visitGuide, RiderGuideActorType actorType, Long actorId) {
        Shop shop = findActiveShop(shopId);
        shopRiderGuideValidator.validate(shop, visitGuide);

        ShopRiderGuide riderGuide = findOrCreate(shopId);
        String previousVisitGuide = riderGuide.getVisitGuide();

        riderGuide.changeVisitGuide(visitGuide);
        shopRiderGuidePersistencePort.save(riderGuide);

        shopRiderGuidePersistencePort.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            actorType,
            actorId,
            RiderGuideActionType.UPDATE,
            previousVisitGuide,
            riderGuide.getVisitGuide(),
            null
        ));

        if (actorType == RiderGuideActorType.CEO) {
            shopChangeHistoryRecorder.record(
                ShopId.of(shopId),
                ShopChangeType.RIDER_VISIT_GUIDE,
                ShopChangeActionType.UPDATE,
                toShopChangeActor(actorType, actorId),
                describeVisitGuide(previousVisitGuide),
                describeVisitGuide(riderGuide.getVisitGuide())
            );
        }
    }

    public void deleteVisitGuide(Long shopId, Long adminId, String reason) {
        findShop(shopId);

        ShopRiderGuide riderGuide = shopRiderGuidePersistencePort.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String previousVisitGuide = riderGuide.getVisitGuide();
        if (previousVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        riderGuide.changeVisitGuide(null);
        shopRiderGuidePersistencePort.save(riderGuide);

        shopRiderGuidePersistencePort.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            RiderGuideActorType.ADMIN,
            adminId,
            RiderGuideActionType.DELETION,
            previousVisitGuide,
            null,
            reason
        ));
    }

    public Long requestRevision(Long shopId, Long adminId, String reason) {
        findShop(shopId);

        ShopRiderGuide riderGuide = shopRiderGuidePersistencePort.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String currentVisitGuide = riderGuide.getVisitGuide();
        if (currentVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        ShopRiderGuideHistory history = shopRiderGuidePersistencePort.saveHistory(ShopRiderGuideHistory.of(
            ShopId.of(shopId),
            RiderGuideActorType.ADMIN,
            adminId,
            RiderGuideActionType.REVISION_REQUEST,
            currentVisitGuide,
            currentVisitGuide,
            reason
        ));
        return history.getId();
    }

    public void updatePickupLocation(
        Long shopId,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        RiderGuideActorType actorType,
        Long actorId
    ) {
        findActiveShop(shopId);

        ShopRiderGuide riderGuide = findOrCreate(shopId);

        String previousValue = describePickupLocation(riderGuide);

        riderGuide.changePickupLocation(roadAddress, lotAddress, detailAddress, latitude, longitude);
        shopRiderGuidePersistencePort.save(riderGuide);

        if (actorType == RiderGuideActorType.CEO) {
            shopChangeHistoryRecorder.record(
                ShopId.of(shopId),
                ShopChangeType.RIDER_PICKUP_LOCATION,
                ShopChangeActionType.UPDATE,
                toShopChangeActor(actorType, actorId),
                previousValue,
                describePickupLocation(riderGuide)
            );
        }
    }

    public void clearPickupLocation(Long shopId, RiderGuideActorType actorType, Long actorId) {
        findActiveShop(shopId);

        shopRiderGuidePersistencePort.findByShopId(ShopId.of(shopId)).ifPresent(riderGuide -> {
            String previousValue = describePickupLocation(riderGuide);

            riderGuide.clearPickupLocation();
            shopRiderGuidePersistencePort.save(riderGuide);

            if (actorType == RiderGuideActorType.CEO) {
                shopChangeHistoryRecorder.record(
                    ShopId.of(shopId),
                    ShopChangeType.RIDER_PICKUP_LOCATION,
                    ShopChangeActionType.DELETE,
                    toShopChangeActor(actorType, actorId),
                    previousValue,
                    null
                );
            }
        });
    }

    private ShopChangeActor toShopChangeActor(RiderGuideActorType actorType, Long actorId) {
        return actorType == RiderGuideActorType.CEO
            ? ShopChangeActor.ceo(actorId)
            : ShopChangeActor.admin(actorId);
    }

    private String describeVisitGuide(String visitGuide) {
        return visitGuide == null || visitGuide.isBlank() ? ShopChangeValueFormatter.unset() : visitGuide;
    }

    private String describePickupLocation(ShopRiderGuide riderGuide) {
        String roadAddress = riderGuide.getPickupRoadAddress();
        if (roadAddress == null || roadAddress.isBlank()) {
            return ShopChangeValueFormatter.unset();
        }
        String detailAddress = riderGuide.getPickupDetailAddress();
        return detailAddress == null || detailAddress.isBlank()
            ? roadAddress
            : roadAddress + " (" + detailAddress + ")";
    }

    private ShopRiderGuide findOrCreate(Long shopId) {
        return shopRiderGuidePersistencePort.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopRiderGuide.of(ShopId.of(shopId)));
    }

    private Shop findShop(Long shopId) {
        return shopPersistencePort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private Shop findActiveShop(Long shopId) {
        Shop shop = findShop(shopId);
        if (shop.isPermanentlyClosed()) {
            throw new BusinessException(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
        }
        return shop;
    }
}
