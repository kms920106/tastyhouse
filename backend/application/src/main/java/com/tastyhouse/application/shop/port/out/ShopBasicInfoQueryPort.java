package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

public interface ShopBasicInfoQueryPort {

    Optional<ShopImageUrlsResult> findShopImageUrls(Long shopId);

    List<ShopOrderMethodResult> findOrderMethods(Long shopId);

    List<ShopBusinessHourResult> findBusinessHours(Long shopId);

    List<ShopBreakTimeResult> findBreakTimes(Long shopId);

    List<ShopClosedDayResult> findClosedDays(Long shopId);

    List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId);

    Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId);

    Optional<ShopOriginInfoResult> findOriginInfo(Long shopId);

    Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId);

    List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId);

    List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId);

    List<ShopBannerImageResult> findBannerImages(Long shopId);

    List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId);
}
