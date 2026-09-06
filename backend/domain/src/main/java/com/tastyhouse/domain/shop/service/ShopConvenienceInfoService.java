package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopConvenienceInfoService {
    private static final double MAX_DISPLAY_LOCATION_DISTANCE_METERS = 1000;

    private final ShopConvenienceInfoRepository shopConvenienceInfoRepository;
    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopConvenienceInfoRepository = shopConvenienceInfoRepository;
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public void upsertConvenienceInfo(
        Long shopId,
        Boolean parkingAvailable,
        Boolean parkingPaid,
        Boolean valetAvailable,
        Boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        ShopChangeActor actor
    ) {
        if (directionsGuide != null) {
            prohibitedWordValidator.validate(directionsGuide);
        }

        if (displayLatitude != null && displayLongitude != null) {
            validateDisplayLocation(shopId, displayLatitude, displayLongitude);
        }

        ShopConvenienceInfo existing = shopConvenienceInfoRepository.findByShopId(shopId).orElse(null);
        String previousValue = describeConvenienceInfo(existing);

        ShopConvenienceInfo shopConvenienceInfo;
        if (existing == null) {
            shopConvenienceInfo = ShopConvenienceInfo.of(
                ShopId.of(shopId),
                parkingAvailable,
                parkingPaid,
                valetAvailable,
                valetPaid,
                directionsGuide,
                displayLatitude,
                displayLongitude
            );
        } else {
            existing.update(
                parkingAvailable,
                parkingPaid,
                valetAvailable,
                valetPaid,
                directionsGuide,
                displayLatitude,
                displayLongitude
            );
            shopConvenienceInfo = existing;
        }

        shopConvenienceInfoRepository.save(shopConvenienceInfo);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONVENIENCE_INFO,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeConvenienceInfo(shopConvenienceInfo)
        );
    }

    public Long assignAmenity(Long shopId, Long amenityCategoryId, ShopChangeActor actor) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));

        ShopAmenity amenity = shopDetailRepository.saveAmenity(
            ShopAmenity.of(ShopId.of(shopId), ShopAmenityCategoryId.of(amenityCategoryId))
        );

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.AMENITY,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeAmenity(amenityCategory)
        );
        return amenity.getId();
    }

    public void unassignAmenity(Long shopId, Long amenityCategoryId, ShopChangeActor actor) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));

        shopDetailRepository.deleteAmenityByShopIdAndCategoryId(shopId, amenityCategoryId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.AMENITY,
            ShopChangeActionType.DELETE,
            actor,
            describeAmenity(amenityCategory),
            null
        );
    }

    private String describeConvenienceInfo(ShopConvenienceInfo convenienceInfo) {
        if (convenienceInfo == null) {
            return ShopChangeValueFormatter.snapshot(List.of());
        }

        List<String> lines = new ArrayList<>(4);
        lines.add("주차: " + describeFacility(convenienceInfo.isParkingAvailable(), convenienceInfo.isParkingPaid()));
        lines.add("발렛: " + describeFacility(convenienceInfo.isValetAvailable(), convenienceInfo.isValetPaid()));
        lines.add("찾아오는길: " + (convenienceInfo.getDirectionsGuide() == null || convenienceInfo.getDirectionsGuide().isBlank()
            ? ShopChangeValueFormatter.unset()
            : convenienceInfo.getDirectionsGuide()));
        lines.add("표시위치: " + describeDisplayLocation(
            convenienceInfo.getDisplayLatitude(), convenienceInfo.getDisplayLongitude()
        ));
        return ShopChangeValueFormatter.snapshot(lines);
    }

    private String describeFacility(boolean available, boolean paid) {
        return available ? "가능(" + (paid ? "유료" : "무료") + ")" : "불가";
    }

    private String describeDisplayLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return ShopChangeValueFormatter.unset();
        }
        return latitude.toPlainString() + ", " + longitude.toPlainString();
    }

    private String describeAmenity(ShopAmenityCategory amenityCategory) {
        String displayName = amenityCategory.getDisplayName();
        return displayName == null || displayName.isBlank()
            ? amenityCategory.getAmenity().getDisplayName()
            : displayName;
    }

    private void validateDisplayLocation(Long shopId, BigDecimal displayLatitude, BigDecimal displayLongitude) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        double distanceMeters = GeoDistance.distanceMeters(
            displayLatitude, displayLongitude, shop.getLatitude(), shop.getLongitude()
        );
        if (distanceMeters > MAX_DISPLAY_LOCATION_DISTANCE_METERS) {
            throw new BusinessException(ErrorCode.SHOP_DISPLAY_LOCATION_OUT_OF_RANGE);
        }
    }
}
