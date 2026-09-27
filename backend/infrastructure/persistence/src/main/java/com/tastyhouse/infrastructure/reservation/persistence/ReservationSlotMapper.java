package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.application.reservation.port.out.write.ReservationSlotState;

final class ReservationSlotMapper {
    private ReservationSlotMapper() {
    }

    static ReservationSlotState toState(ReservationSlotJpaEntity entity) {
        return new ReservationSlotState(
            entity.getId(),
            entity.getShopId(),
            entity.getSlotDate(),
            entity.getSlotTime(),
            entity.getCapacity(),
            entity.getReservedCount(),
            entity.getVersion()
        );
    }

    static ReservationSlotJpaEntity toEntity(ReservationSlotState state) {
        return ReservationSlotJpaEntity.create(
            state.shopId(),
            state.slotDate(),
            state.slotTime(),
            state.capacity(),
            state.reservedCount()
        );
    }

    static void applyChanges(ReservationSlotJpaEntity entity, ReservationSlotState state) {
        entity.applyChanges(state.reservedCount());
    }
}
