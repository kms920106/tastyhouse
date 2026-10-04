package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopDeliveryResolution;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

@Service
public class ShopOrderContextService {

    private final ShopPersistencePort shopPersistencePort;
    private final ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort;
    private final ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final ScheduledOrderSlotService scheduledOrderSlotService;

    public ShopOrderContextService(
        ShopPersistencePort shopPersistencePort,
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        this.shopPersistencePort = shopPersistencePort;
        this.shopDeliveryAreaPersistencePort = shopDeliveryAreaPersistencePort;
        this.shopDeliveryTipPersistencePort = shopDeliveryTipPersistencePort;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
    }

    public OrderableShop loadOrderableShop(ShopId shopId, OrderMethod orderMethod, LocalDateTime at) {
        Shop shop = shopPersistencePort.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
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
            shopDeliveryTipPersistencePort.findSettingByShopId(shopId).orElse(null),
            shopDeliveryTipPersistencePort.findTiersByShopId(shopId),
            shopDeliveryTipPersistencePort.findRegionTipsByShopId(shopId),
            shopDeliveryTipPersistencePort.findScheduleTipsByShopId(shopId),
            shopDeliveryTipPersistencePort.findHolidayTipByShopId(shopId).orElse(null)
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
            throw new ApplicationException(WebErrorCode.SHOP_SCHEDULED_ORDER_DISABLED);
        }
        if (!ScheduledOrderPolicy.supports(orderMethod)) {
            throw new ApplicationException(WebErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED,
                WebErrorCode.ORDER_SCHEDULE_METHOD_NOT_SUPPORTED.getDefaultMessage() + ": " + orderMethod);
        }

        return scheduledOrderSlotService.resolveSlot(shopId, orderMethod, scheduledAt, at);
    }

    private void validateDeliveryArea(ShopId shopId, AdminDongId adminDongId) {
        if (shopDeliveryAreaPersistencePort.countByShopId(shopId) == 0) {
            return;
        }
        if (adminDongId == null
            || !shopDeliveryAreaPersistencePort.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new ApplicationException(WebErrorCode.ORDER_DELIVERY_AREA_NOT_COVERED);
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
