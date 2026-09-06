package com.tastyhouse.domain.shop.service;

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
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ScheduledOrderSlotService {
    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;
    private final ShopSuspensionRepository shopSuspensionRepository;
    private final ScheduledOrderSlotCalculator scheduledOrderSlotCalculator;

    public ScheduledOrderSlotService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.shopTemporaryClosureRepository = shopTemporaryClosureRepository;
        this.shopSuspensionRepository = shopSuspensionRepository;
        this.scheduledOrderSlotCalculator = scheduledOrderSlotCalculator;
    }

    public List<ScheduledOrderSlot> findAvailableSlots(ShopId shopId, OrderMethod orderMethod, LocalDateTime now) {
        Shop shop = shopRepository.findById(shopId)
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
        List<ShopBusinessHour> businessHours = shopDetailRepository.findBusinessHoursByShopId(rawShopId);
        List<ShopBreakTime> breakTimes = shopDetailRepository.findBreakTimesByShopId(rawShopId);
        List<ShopClosedDay> closedDays = shopDetailRepository.findClosedDaysByShopId(rawShopId);
        List<ShopTemporaryClosure> temporaryClosures = shopTemporaryClosureRepository.findByShopId(rawShopId);
        List<ShopSuspension> suspensions = shopSuspensionRepository.findByShopId(rawShopId);
        List<ShopOrderMethod> shopOrderMethods = shopDetailRepository.findOrderMethodsByShopId(rawShopId);

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
