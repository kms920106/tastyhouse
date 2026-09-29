package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationPersistencePort;

import static com.tastyhouse.infrastructure.reservation.persistence.QReservationJpaEntity.reservationJpaEntity;

@Repository
public class ReservationPersistenceAdapter implements ReservationPersistencePort {
    private final JPAQueryFactory queryFactory;
    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationPersistenceAdapter(JPAQueryFactory queryFactory, ReservationJpaRepository reservationJpaRepository) {
        this.queryFactory = queryFactory;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return reservationJpaRepository.findById(id.value()).map(ReservationMapper::toDomain);
    }

    @Override
    public boolean existsBlockingByMemberShopDate(MemberId memberId, ShopId shopId, LocalDate date) {
        List<String> blockingStatuses = ReservationStatus.blockingStatuses().stream()
            .map(ReservationStatus::name)
            .toList();
        return queryFactory.selectOne()
            .from(reservationJpaEntity)
            .where(
                reservationJpaEntity.memberId.eq(memberId.value()),
                reservationJpaEntity.shopId.eq(shopId.value()),
                reservationJpaEntity.reservationDate.eq(date),
                reservationJpaEntity.status.in(blockingStatuses)
            )
            .fetchFirst() != null;
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            ReservationJpaEntity saved = reservationJpaRepository.save(ReservationMapper.toEntity(reservation));
            return ReservationMapper.toDomain(saved);
        }

        ReservationJpaEntity entity = reservationJpaRepository.findById(reservation.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 예약입니다: " + reservation.getId()));
        ReservationMapper.applyChanges(entity, reservation);
        return ReservationMapper.toDomain(entity);
    }
}
