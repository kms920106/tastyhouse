package com.tastyhouse.domain.shop.service;

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
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRiderGuideService {
    private final ShopRiderGuideRepository shopRiderGuideRepository;
    private final ShopRepository shopRepository;
    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopRiderGuideService(
        ShopRiderGuideRepository shopRiderGuideRepository,
        ShopRepository shopRepository,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopRiderGuideRepository = shopRiderGuideRepository;
        this.shopRepository = shopRepository;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public void updateVisitGuide(Long shopId, String visitGuide, RiderGuideActorType actorType, Long actorId) {
        Shop shop = findActiveShop(shopId);
        shopRiderGuideValidator.validate(shop, visitGuide);

        ShopRiderGuide riderGuide = findOrCreate(shopId);
        String previousVisitGuide = riderGuide.getVisitGuide();

        riderGuide.changeVisitGuide(visitGuide);
        shopRiderGuideRepository.save(riderGuide);

        shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
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

        ShopRiderGuide riderGuide = shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String previousVisitGuide = riderGuide.getVisitGuide();
        if (previousVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        riderGuide.changeVisitGuide(null);
        shopRiderGuideRepository.save(riderGuide);

        shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
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

        ShopRiderGuide riderGuide = shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND));

        String currentVisitGuide = riderGuide.getVisitGuide();
        if (currentVisitGuide == null) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
        }

        ShopRiderGuideHistory history = shopRiderGuideRepository.saveHistory(ShopRiderGuideHistory.of(
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
        shopRiderGuideRepository.save(riderGuide);

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

        shopRiderGuideRepository.findByShopId(ShopId.of(shopId)).ifPresent(riderGuide -> {
            String previousValue = describePickupLocation(riderGuide);

            riderGuide.clearPickupLocation();
            shopRiderGuideRepository.save(riderGuide);

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
        return shopRiderGuideRepository.findByShopId(ShopId.of(shopId))
            .orElseGet(() -> ShopRiderGuide.of(ShopId.of(shopId)));
    }

    private Shop findShop(Long shopId) {
        return shopRepository.findById(ShopId.of(shopId))
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
