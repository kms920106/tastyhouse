package com.tastyhouse.core.domain.reservation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationSlotRepository;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.reservation.application.dto.command.ReservationCreateCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.exception.OptimisticLockConflictException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private static final int MAX_RETRY = 3;

    private final ReservationCreator reservationCreator;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;
    private final ShopQueryService shopQueryService;

    /**
     * 예약 생성 (비트랜잭션, 낙관적 락 재시도 루프).
     * 일시적 경합(슬롯 동시 insert/낙관적 락 충돌)만 재시도하고,
     * 비즈니스 예외(SLOT_FULL/DUPLICATE/TERMS 등)는 재시도 없이 즉시 전파한다.
     */
    public ReservationResult create(MemberId memberId, ReservationCreateCommand cmd) {
        for (int attempt = 0; ; attempt++) {
            try {
                return reservationCreator.createInNewTx(memberId, cmd);
            } catch (OptimisticLockConflictException | DataIntegrityViolationException e) {
                log.warn("예약 생성 동시성 경합 재시도 {}/{}: shopId={}, date={}, time={}",
                    attempt + 1, MAX_RETRY, cmd.shopId(), cmd.date(), cmd.time());
                if (attempt == MAX_RETRY - 1) {
                    // 재시도 소진 = 사실상 마감으로 간주
                    throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
                }
            }
        }
    }

    /**
     * 점주 승인: PENDING -> CONFIRMED
     * TODO(보안): Shop-owner 연결 후 점주 본인 검증(shop.getOwnerId() == currentMemberId) 추가 필요.
     */
    @Transactional
    public ReservationResult confirm(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.confirm();
        reservationRepository.save(reservation);
        return toResult(reservation);
    }

    /**
     * 점주 거절: PENDING -> REJECTED (슬롯 정원 반납)
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public ReservationResult reject(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
        return toResult(reservation);
    }

    /**
     * 방문 완료: CONFIRMED -> COMPLETED
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public ReservationResult complete(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.complete();
        reservationRepository.save(reservation);
        return toResult(reservation);
    }

    /**
     * 사용자 취소: PENDING|CONFIRMED -> CANCELED (슬롯 정원 반납)
     */
    @Transactional
    public ReservationResult cancel(ReservationId reservationId, MemberId memberId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
        return toResult(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약의 슬롯 좌표(shopId, date, time)로 슬롯을 역추적해 정원 반납.
     */
    private void releaseSlot(Reservation reservation) {
        slotRepository
            .findByShopAndDateAndTime(reservation.getShopId(), reservation.getReservationDate(), reservation.getReservationTime())
            .ifPresent(slot -> {
                slot.release();
                slotRepository.save(slot);
            });
    }

    private ReservationResult toResult(Reservation reservation) {
        Shop shop = shopQueryService.findShopById(ShopId.of(reservation.getShopId()));
        String shopImageUrl = shopQueryService.findThumbnailFilePath(shop.getThumbnailImageFileId())
            .orElse(null);
        return ReservationResult.from(
            reservation,
            shop.getName(),
            shopImageUrl,
            shop.getRoadAddress(),
            shop.getLotAddress()
        );
    }
}
