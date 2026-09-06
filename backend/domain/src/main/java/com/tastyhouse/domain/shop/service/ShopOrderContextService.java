package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopOrderContextService {
    private final ShopRepository shopRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final ScheduledOrderSlotService scheduledOrderSlotService;

    public ShopOrderContextService(
        ShopRepository shopRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        this.shopRepository = shopRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
    }

    public OrderableShop loadOrderableShop(ShopId shopId, OrderMethod orderMethod, LocalDateTime at) {
        Shop shop = shopRepository.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shopOrderAvailabilityService.validateOrderable(shop, orderMethod, at);
        return new OrderableShop(shop);
    }

    public void validateMinOrderAmount(
        OrderableShop shop,
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount
    ) {
        shop.value().validateMinOrderAmount(orderMethod, orderAmountAfterProductDiscount);
    }

    public ShopDeliveryResolution resolveDelivery(
        OrderableShop shop,
        ShopId shopId,
        DeliveryDestinationSpec address,
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        validateDeliveryArea(shopId, address.adminDongId());

        double meters = GeoDistance.distanceMeters(
            shop.value().getLatitude(), shop.value().getLongitude(), address.latitude(), address.longitude()
        );

        ShopDeliveryTipBreakdown breakdown = shopDeliveryTipCalculator.calculate(ShopDeliveryTipContext.of(
            orderMethod,
            orderAmountAfterProductDiscount,
            meters,
            address.adminDongId(),
            orderedAt,
            publicHoliday,
            shopDeliveryTipRepository.findSettingByShopId(shopId).orElse(null),
            shopDeliveryTipRepository.findTiersByShopId(shopId),
            shopDeliveryTipRepository.findRegionTipsByShopId(shopId),
            shopDeliveryTipRepository.findScheduleTipsByShopId(shopId),
            shopDeliveryTipRepository.findHolidayTipByShopId(shopId).orElse(null)
        ));

        return new ShopDeliveryResolution((int) Math.round(meters), breakdown);
    }

    public ScheduledOrderSlot resolveScheduledSlot(
        OrderableShop shop,
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime scheduledAt,
        LocalDateTime at
    ) {
        if (!shop.value().isScheduledOrderEnabled()) {
            throw new BusinessException(ErrorCode.SHOP_SCHEDULED_ORDER_DISABLED);
        }
        if (!ScheduledOrderPolicy.supports(orderMethod)) {
            throw new BusinessException(ErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED,
                ErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED.getDefaultMessage() + ": " + orderMethod);
        }

        return scheduledOrderSlotService.resolveSlot(shopId, orderMethod, scheduledAt, at);
    }

    private void validateDeliveryArea(ShopId shopId, AdminDongId adminDongId) {
        if (shopDeliveryAreaRepository.countByShopId(shopId) == 0) {
            return;
        }
        if (adminDongId == null
            || !shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.ORDER_DELIVERY_AREA_NOT_COVERED);
        }
    }

    public static final class OrderableShop {
        private final Shop shop;

        private OrderableShop(Shop shop) {
            this.shop = shop;
        }

        Shop value() {
            return this.shop;
        }
    }

    public record DeliveryDestinationSpec(
        AdminDongId adminDongId,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        public static DeliveryDestinationSpec of(
            AdminDongId adminDongId,
            BigDecimal latitude,
            BigDecimal longitude
        ) {
            return new DeliveryDestinationSpec(adminDongId, latitude, longitude);
        }
    }
}
