package com.tastyhouse.application.reservation.store;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationStatePort;

public class ReservationStore implements ReservationRepository {
    private final ReservationStatePort reservationStatePort;

    public ReservationStore(ReservationStatePort reservationStatePort) {
        this.reservationStatePort = reservationStatePort;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return reservationStatePort.findById(id.value()).map(ReservationStateMapper::toDomain);
    }

    @Override
    public boolean existsBlockingByMemberShopDate(MemberId memberId, ShopId shopId, LocalDate date) {
        List<String> blockingStatuses = ReservationStatus.blockingStatuses().stream()
            .map(ReservationStatus::name)
            .toList();
        return reservationStatePort.existsBlockingByMemberShopDate(memberId.value(), shopId.value(), date, blockingStatuses);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return ReservationStateMapper.toDomain(reservationStatePort.save(ReservationStateMapper.toState(reservation)));
    }
}
