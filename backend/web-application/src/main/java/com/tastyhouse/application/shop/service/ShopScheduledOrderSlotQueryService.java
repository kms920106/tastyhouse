package com.tastyhouse.application.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopScheduledOrderSlotQueryUseCase;
import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotItemResult;
import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotsViewResult;

@Service
@Transactional(readOnly = true)
class ShopScheduledOrderSlotQueryService implements ShopScheduledOrderSlotQueryUseCase {

    private final ScheduledOrderSlotService scheduledOrderSlotService;

    public ShopScheduledOrderSlotQueryService(ScheduledOrderSlotService scheduledOrderSlotService) {
        this.scheduledOrderSlotService = scheduledOrderSlotService;
    }

    @Override
    public ScheduledOrderSlotsViewResult getScheduledOrderSlots(Long shopId, String orderMethod) {
        OrderMethod method = OrderMethod.from(orderMethod);
        List<ScheduledOrderSlot> slots = scheduledOrderSlotService.findAvailableSlots(
            ShopId.of(shopId), method, LocalDateTime.now()
        );

        int leadTimeMinutes = ScheduledOrderPolicy.supports(method)
            ? ScheduledOrderPolicy.leadTimeMinutes(method)
            : 0;

        return new ScheduledOrderSlotsViewResult(
            !slots.isEmpty(),
            leadTimeMinutes,
            ScheduledOrderPolicy.SLOT_UNIT_MINUTES,
            ScheduledOrderPolicy.isRangeSlot(method),
            slots.stream().map(slot -> toScheduledOrderSlotItemResult(slot, method)).toList()
        );
    }

    private ScheduledOrderSlotItemResult toScheduledOrderSlotItemResult(
        ScheduledOrderSlot slot,
        OrderMethod orderMethod
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
        String startLabel = slot.startAt().format(formatter);
        String label = ScheduledOrderPolicy.isRangeSlot(orderMethod)
            ? startLabel + "~" + slot.endAt().format(formatter)
            : startLabel;

        return new ScheduledOrderSlotItemResult(
            slot.startAt(),
            slot.endAt(),
            label,
            toDayLabel(slot.startAt())
        );
    }

    private String toDayLabel(LocalDateTime slotStartAt) {
        return slotStartAt.toLocalDate().isEqual(LocalDate.now()) ? "오늘" : "내일";
    }
}
