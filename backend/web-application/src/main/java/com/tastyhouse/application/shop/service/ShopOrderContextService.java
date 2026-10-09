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
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;

@Service
public class ShopOrderContextService {

    private final ShopLoadPort shopLoadPort;
    private final ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort;
    private final ShopDeliveryTipLoadPort shopDeliveryTipLoadPort;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final ScheduledOrderSlotService scheduledOrderSlotService;

    public ShopOrderContextService(
        ShopLoadPort shopLoadPort,
        ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort,
        ShopDeliveryTipLoadPort shopDeliveryTipLoadPort,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        this.shopLoadPort = shopLoadPort;
        this.shopDeliveryAreaLoadPort = shopDeliveryAreaLoadPort;
        this.shopDeliveryTipLoadPort = shopDeliveryTipLoadPort;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
    }

    public OrderableShop loadOrderableShop(ShopId shopId, OrderMethod orderMethod, LocalDateTime at) {
        Shop shop = shopLoadPort.findVisibleById(shopId)
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
            shopDeliveryTipLoadPort.findSettingByShopId(shopId).orElse(null),
            shopDeliveryTipLoadPort.findTiersByShopId(shopId),
            shopDeliveryTipLoadPort.findRegionTipsByShopId(shopId),
            shopDeliveryTipLoadPort.findScheduleTipsByShopId(shopId),
            shopDeliveryTipLoadPort.findHolidayTipByShopId(shopId).orElse(null)
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
        if (shopDeliveryAreaLoadPort.countByShopId(shopId) == 0) {
            return;
        }
        if (adminDongId == null
            || !shopDeliveryAreaLoadPort.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
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
