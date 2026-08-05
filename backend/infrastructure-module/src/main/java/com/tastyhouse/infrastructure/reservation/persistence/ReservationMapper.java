package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 예약 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ReservationMapper {

    private ReservationMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getReservationDate(),
            entity.getReservationTime(),
            entity.getPartySize(),
            entity.getStatus(),
            entity.getRequest(),
            entity.getCreatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ReservationJpaEntity toEntity(Reservation domain) {
        return ReservationJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getReservationDate(),
            domain.getReservationTime(),
            domain.getPartySize(),
            domain.getStatus(),
            domain.getRequest()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ReservationJpaEntity entity, Reservation domain) {
        entity.applyChanges(domain.getStatus());
    }
}
