package com.tastyhouse.core.domain.reservation.application;

import com.tastyhouse.core.domain.reservation.application.dto.command.CreateReservationCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.reservation.domain.repository.ShopReservationSlotRepository;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private static final int MAX_RETRY = 3;

    private final ReservationCreator reservationCreator;
    private final ReservationRepository reservationRepository;
    private final ShopReservationSlotRepository slotRepository;
    private final ShopQueryService shopQueryService;

    /**
     * 예약 생성 (비트랜잭션, 낙관적 락 재시도 루프).
     * 일시적 경합(슬롯 동시 insert/낙관적 락 충돌)만 재시도하고,
     * 비즈니스 예외(SLOT_FULL/DUPLICATE/TERMS 등)는 재시도 없이 즉시 전파한다.
     */
    public ReservationResult create(Long memberId, CreateReservationCommand cmd) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                return reservationCreator.createInNewTx(memberId, cmd);
            } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
                log.warn("예약 생성 동시성 경합 재시도 {}/{}: shopId={}, date={}, time={}",
                    attempt + 1, MAX_RETRY, cmd.shopId(), cmd.date(), cmd.time());
                if (attempt == MAX_RETRY - 1) {
                    // 재시도 소진 = 사실상 마감으로 간주
                    throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
                }
            }
        }
        // 도달 불가 (위 루프에서 반환 또는 예외)
        throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
    }

    /**
     * 점주 승인: PENDING -> CONFIRMED
     * TODO(보안): Shop-owner 연결 후 점주 본인 검증(shop.getOwnerId() == currentMemberId) 추가 필요.
     */
    @Transactional
    public ReservationResult confirm(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.confirm();
        return toResult(reservation);
    }

    /**
     * 점주 거절: PENDING -> REJECTED (슬롯 정원 반납)
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public ReservationResult reject(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        releaseSlot(reservation);
        return toResult(reservation);
    }

    /**
     * 방문 완료: CONFIRMED -> COMPLETED
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public ReservationResult complete(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.complete();
        return toResult(reservation);
    }

    /**
     * 사용자 취소: PENDING|CONFIRMED -> CANCELED (슬롯 정원 반납)
     */
    @Transactional
    public ReservationResult cancel(Long memberId, Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        releaseSlot(reservation);
        return toResult(reservation);
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약의 슬롯 좌표(shopId, date, time)로 슬롯을 역추적해 정원 반납.
     */
    private void releaseSlot(Reservation reservation) {
        slotRepository
            .findByShopAndDateAndTime(reservation.getShopId(), reservation.getReservationDate(), reservation.getReservationTime())
            .ifPresent(ShopReservationSlot::release);
    }

    private ReservationResult toResult(Reservation reservation) {
        String shopName = shopQueryService.findShopById(reservation.getShopId()).getName();
        return ReservationResult.from(reservation, shopName);
    }
}
