package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlotContext;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureLoadPort;

@Service
public class ScheduledOrderSlotService {

    private final ShopLoadPort shopLoadPort;
    private final ShopBusinessHourLoadPort shopBusinessHourLoadPort;
    private final ShopOrderMethodLoadPort shopOrderMethodLoadPort;
    private final ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort;
    private final ShopSuspensionLoadPort shopSuspensionLoadPort;
    private final ScheduledOrderSlotCalculator scheduledOrderSlotCalculator;

    public ScheduledOrderSlotService(
        ShopLoadPort shopLoadPort,
        ShopBusinessHourLoadPort shopBusinessHourLoadPort,
        ShopOrderMethodLoadPort shopOrderMethodLoadPort,
        ShopTemporaryClosureLoadPort shopTemporaryClosureLoadPort,
        ShopSuspensionLoadPort shopSuspensionLoadPort,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        this.shopLoadPort = shopLoadPort;
        this.shopBusinessHourLoadPort = shopBusinessHourLoadPort;
        this.shopOrderMethodLoadPort = shopOrderMethodLoadPort;
        this.shopTemporaryClosureLoadPort = shopTemporaryClosureLoadPort;
        this.shopSuspensionLoadPort = shopSuspensionLoadPort;
        this.scheduledOrderSlotCalculator = scheduledOrderSlotCalculator;
    }

    public List<ScheduledOrderSlot> findAvailableSlots(ShopId shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopLoadPort.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));

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
            .orElseThrow(() -> new ApplicationException(WebErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE,
                WebErrorCode.ORDER_SCHEDULED_AT_UNAVAILABLE.getDefaultMessage() + ": " + scheduledAt));
    }

    private ScheduledOrderSlotContext buildContext(
        Shop shop,
        ShopId shopId,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        Long rawShopId = shopId.value();
        List<ShopBusinessHour> businessHours = shopBusinessHourLoadPort.findBusinessHoursByShopId(rawShopId);
        List<ShopBreakTime> breakTimes = shopBusinessHourLoadPort.findBreakTimesByShopId(rawShopId);
        List<ShopClosedDay> closedDays = shopBusinessHourLoadPort.findClosedDaysByShopId(rawShopId);
        List<ShopTemporaryClosure> temporaryClosures = shopTemporaryClosureLoadPort.findByShopId(rawShopId);
        List<ShopSuspension> suspensions = shopSuspensionLoadPort.findByShopId(rawShopId);
        List<ShopOrderMethod> shopOrderMethods = shopOrderMethodLoadPort.findOrderMethodsByShopId(rawShopId);

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
