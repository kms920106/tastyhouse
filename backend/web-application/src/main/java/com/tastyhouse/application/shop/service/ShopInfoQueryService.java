package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopInfoQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClassificationQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopInfoViewResult;

@Service
@Transactional(readOnly = true)
class ShopInfoQueryService implements ShopInfoQueryUseCase {

    private final ShopVisibleReader shopVisibleReader;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopClassificationQueryPort shopClassificationQueryPort;

    public ShopInfoQueryService(
        ShopVisibleReader shopVisibleReader,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopClassificationQueryPort shopClassificationQueryPort
    ) {
        this.shopVisibleReader = shopVisibleReader;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopClassificationQueryPort = shopClassificationQueryPort;
    }

    @Override
    public ShopInfoViewResult getShopInfo(Long shopId) {
        shopVisibleReader.findVisibleShop(shopId);
        List<ShopBusinessHourResult> businessHours =
            ShopCodeDescriptions.ofBusinessHours(shopBasicInfoQueryPort.findBusinessHours(shopId));
        List<ShopBreakTimeResult> breakTimes = ShopCodeDescriptions.ofBreakTimes(shopBasicInfoQueryPort.findBreakTimes(shopId));
        List<ShopClosedDayResult> closedDays = ShopCodeDescriptions.ofClosedDays(shopBasicInfoQueryPort.findClosedDays(shopId));
        List<ShopAmenityWithCategoryResult> shopAmenities = shopClassificationQueryPort.findAmenitiesWithCategory(shopId);

        String ownerMessage = null;
        LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = shopBasicInfoQueryPort.findLatestOwnerMessage(shopId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().message();
            ownerMessageCreatedAt = ownerMessageHistory.get().createdAt();
        }

        Boolean parkingAvailable = null;
        Boolean parkingPaid = null;
        Boolean valetAvailable = null;
        Boolean valetPaid = null;
        String directionsGuide = null;
        BigDecimal displayLatitude = null;
        BigDecimal displayLongitude = null;
        var convenienceInfo = shopBasicInfoQueryPort.findConvenienceInfo(shopId);
        if (convenienceInfo.isPresent()) {
            ShopConvenienceInfoResult info = convenienceInfo.get();
            parkingAvailable = info.parkingAvailable();
            parkingPaid = info.parkingPaid();
            valetAvailable = info.valetAvailable();
            valetPaid = info.valetPaid();
            directionsGuide = info.directionsGuide();
            displayLatitude = info.displayLatitude();
            displayLongitude = info.displayLongitude();
        }

        return new ShopInfoViewResult(
            closedDays,
            businessHours,
            breakTimes,
            shopAmenities,
            ownerMessage,
            ownerMessageCreatedAt,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude
        );
    }
}
