package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotContext;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosurePersistencePort;

public class ScheduledOrderSlotService {
    private final ShopPersistencePort shopPersistencePort;
    private final ShopDetailPersistencePort shopDetailPersistencePort;
    private final ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort;
    private final ShopSuspensionPersistencePort shopSuspensionPersistencePort;
    private final ScheduledOrderSlotCalculator scheduledOrderSlotCalculator;

    public ScheduledOrderSlotService(
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort,
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        this.shopPersistencePort = shopPersistencePort;
        this.shopDetailPersistencePort = shopDetailPersistencePort;
        this.shopTemporaryClosurePersistencePort = shopTemporaryClosurePersistencePort;
        this.shopSuspensionPersistencePort = shopSuspensionPersistencePort;
        this.scheduledOrderSlotCalculator = scheduledOrderSlotCalculator;
    }

    public List<ScheduledOrderSlot> findAvailableSlots(ShopId shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopPersistencePort.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        return scheduledOrderSlotCalculator.calculate(buildContext(shop, shopId, orderMethod, now));
    }

    public ScheduledOrderSlot resolveSlot(
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime scheduledAt,
        LocalDateTime now
    ) {
        return findAvailableSlots(shopId, orderMethod, now).stream()
            .filter(slot -> slot.matches(scheduledAt))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE,
                ErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE.getDefaultMessage() + ": " + scheduledAt));
    }

    private ScheduledOrderSlotContext buildContext(
        Shop shop,
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        Long rawShopId = shopId.value();
        List<ShopBusinessHour> businessHours = shopDetailPersistencePort.findBusinessHoursByShopId(rawShopId);
        List<ShopBreakTime> breakTimes = shopDetailPersistencePort.findBreakTimesByShopId(rawShopId);
        List<ShopClosedDay> closedDays = shopDetailPersistencePort.findClosedDaysByShopId(rawShopId);
        List<ShopTemporaryClosure> temporaryClosures = shopTemporaryClosurePersistencePort.findByShopId(rawShopId);
        List<ShopSuspension> suspensions = shopSuspensionPersistencePort.findByShopId(rawShopId);
        List<ShopOrderMethod> shopOrderMethods = shopDetailPersistencePort.findOrderMethodsByShopId(rawShopId);

        return ScheduledOrderSlotContext.of(
            shop,
            orderMethod,
            now,
            businessHours,
            breakTimes,
            closedDays,
            temporaryClosures,
            suspensions,
            shopOrderMethods
        );
    }
}
