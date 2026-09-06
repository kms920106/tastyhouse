package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.in.ShopPriceBadgeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopPriceBadgeViewResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class ShopPriceBadgeQueryService implements ShopPriceBadgeQueryUseCase {

    private static final int BUSINESS_DAY_WINDOW_DAYS = 7;

    private static final boolean PUBLIC_HOLIDAY = false;

    private final ProductQueryPort productQueryPort;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final StorePriceVerificationReader storePriceVerificationReader;
    private final StorePriceBadgePolicy storePriceBadgePolicy;
    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopPriceBadgeQueryService(
        ProductQueryPort productQueryPort,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        StorePriceVerificationReader storePriceVerificationReader,
        StorePriceBadgePolicy storePriceBadgePolicy,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        this.productQueryPort = productQueryPort;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.storePriceVerificationReader = storePriceVerificationReader;
        this.storePriceBadgePolicy = storePriceBadgePolicy;
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    @Override
    public ShopPriceBadgeViewResult getPriceBadges(Long shopId) {
        LocalDateTime now = LocalDateTime.now();

        boolean sameAsStorePrice = storePriceBadgePolicy.shouldExposeSameAsStorePriceBadge(
            storePriceVerificationReader.readVerified(shopId));

        List<ProductPrice> prices = productQueryPort.findShopProductPrices(shopId).stream()
            .map(ShopPriceBadgeQueryService::toProductPrice)
            .toList();
        boolean storePricePickup = storePriceBadgePolicy.shouldExposePickupBadge(
            prices,
            productQueryPort.countVisibleProducts(shopId),
            findPassedBusinessDates(shopId, now),
            now
        );

        return new ShopPriceBadgeViewResult(sameAsStorePrice, storePricePickup);
    }

    private List<LocalDate> findPassedBusinessDates(Long shopId, LocalDateTime now) {
        List<ShopBusinessHour> businessHours = shopBasicInfoQueryPort.findBusinessHours(shopId).stream()
            .map(businessHour -> toShopBusinessHour(shopId, businessHour))
            .toList();
        if (businessHours.isEmpty()) {
            return List.of();
        }

        List<ShopClosedDayResult> closedDays = shopBasicInfoQueryPort.findClosedDays(shopId);
        LocalDate today = now.toLocalDate();
        List<LocalDate> businessDates = new ArrayList<>();
        for (int offset = BUSINESS_DAY_WINDOW_DAYS; offset >= 1; offset--) {
            LocalDate candidate = today.minusDays(offset);
            if (isBusinessDate(businessHours, closedDays, candidate)) {
                businessDates.add(candidate);
            }
        }
        return businessDates;
    }

    private boolean isBusinessDate(
        List<ShopBusinessHour> businessHours,
        List<ShopClosedDayResult> closedDays,
        LocalDate date
    ) {
        boolean closedDay = closedDays.stream()
            .anyMatch(closed -> closed.closedDayType() != null && closed.closedDayType().matches(date));
        if (closedDay) {
            return false;
        }

        ShopBusinessHour hour = shopOperatingStatusCalculator.selectApplicableHour(
            businessHours, date.getDayOfWeek(), PUBLIC_HOLIDAY);
        return hour != null && !hour.isClosed();
    }

    private static ShopBusinessHour toShopBusinessHour(Long shopId, ShopBusinessHourResult dto) {
        return ShopBusinessHour.reconstitute(
            dto.id(),
            ShopId.of(shopId),
            dto.dayType(),
            dto.openTime(),
            dto.closeTime(),
            dto.closed(),
            dto.allDay()
        );
    }

    private static ProductPrice toProductPrice(ProductPriceResult dto) {
        return ProductPrice.reconstitute(
            dto.id(),
            ProductId.of(dto.productId()),
            dto.priceName(),
            dto.deliveryPrice(),
            dto.storePrice(),
            dto.pickupPrice(),
            dto.sort(),
            dto.pickupPriceSetAt(),
            null,
            null
        );
    }
}
