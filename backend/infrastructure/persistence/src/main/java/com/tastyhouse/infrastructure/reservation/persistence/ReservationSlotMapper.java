package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 예약 슬롯 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReservationSlotMapper {

    private ReservationSlotMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로). 낙관적 락 버전({@code version})도 함께 복원한다.
     */
    static ReservationSlot toDomain(ReservationSlotJpaEntity entity) {
        return ReservationSlot.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getSlotDate(),
            entity.getSlotTime(),
            entity.getCapacity(),
            entity.getReservedCount(),
            entity.getVersion()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자·버전 없는 상태).
     */
    static ReservationSlotJpaEntity toEntity(ReservationSlot domain) {
        return ReservationSlotJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getSlotDate(),
            domain.getSlotTime(),
            domain.getCapacity(),
            domain.getReservedCount()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체). 버전은 flush 시 JPA가 자동 검증·증가시킨다.
     */
    static void applyChanges(ReservationSlotJpaEntity entity, ReservationSlot domain) {
        entity.applyChanges(domain.getReservedCount());
    }
}
