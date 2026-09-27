package com.tastyhouse.application.reservation.store;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotState;

final class ReservationSlotStateMapper {
    private ReservationSlotStateMapper() {
    }

    static ReservationSlot toDomain(ReservationSlotState state) {
        return ReservationSlot.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.slotDate(),
            state.slotTime(),
            state.capacity(),
            state.reservedCount(),
            state.version()
        );
    }

    static ReservationSlotState toState(ReservationSlot slot) {
        return new ReservationSlotState(
            slot.getId(),
            slot.getShopId() == null ? null : slot.getShopId().value(),
            slot.getSlotDate(),
            slot.getSlotTime(),
            slot.getCapacity(),
            slot.getReservedCount(),
            slot.getVersion()
        );
    }
}
