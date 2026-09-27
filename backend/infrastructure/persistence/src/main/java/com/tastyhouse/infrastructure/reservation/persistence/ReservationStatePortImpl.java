package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.reservation.port.out.write.ReservationState;
import com.tastyhouse.application.reservation.port.out.write.ReservationStatePort;

import static com.tastyhouse.infrastructure.reservation.persistence.QReservationJpaEntity.reservationJpaEntity;

@Repository
public class ReservationStatePortImpl implements ReservationStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationStatePortImpl(JPAQueryFactory queryFactory, ReservationJpaRepository reservationJpaRepository) {
        this.queryFactory = queryFactory;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<ReservationState> findById(Long id) {
        return reservationJpaRepository.findById(id).map(ReservationMapper::toState);
    }

    @Override
    public boolean existsBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date, Collection<String> blockingStatuses) {
        return queryFactory.selectOne()
            .from(reservationJpaEntity)
            .where(
                reservationJpaEntity.memberId.eq(memberId),
                reservationJpaEntity.shopId.eq(shopId),
                reservationJpaEntity.reservationDate.eq(date),
                reservationJpaEntity.status.in(blockingStatuses)
            )
            .fetchFirst() != null;
    }

    @Override
    public ReservationState save(ReservationState state) {
        if (state.id() == null) {
            ReservationJpaEntity saved = reservationJpaRepository.save(ReservationMapper.toEntity(state));
            return ReservationMapper.toState(saved);
        }

        ReservationJpaEntity entity = reservationJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 예약입니다: " + state.id()));
        ReservationMapper.applyChanges(entity, state);
        return ReservationMapper.toState(entity);
    }
}
