package com.tastyhouse.webapi.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.reservation.domain.service.ReservationBookingService;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.exception.OptimisticLockConflictException;

/**
 * 예약 명령 서비스(web).
 *
 * <p>두 종류의 명령을 다룬다.
 * <ul>
 *   <li><b>단일 애그리거트 상태전이</b>({@code confirm}/{@code complete}) — 예약 하나만 바꾸므로 write
 *       포트를 직접 주입해 이 서비스가 처리한다(분류 A). 도메인 모델이 순수 POJO라 더티 체킹이 없으므로
 *       변경 후 명시적으로 {@code save}를 호출한다.</li>
 *   <li><b>슬롯 정원이 함께 움직이는 연산</b>({@code create}/{@code cancel}/{@code reject}) — 예약과 슬롯
 *       두 애그리거트의 원자 불변식이므로 도메인 서비스
 *       {@link ReservationBookingService}에 위임한다(분류 C).</li>
 * </ul>
 *
 * <p><b>낙관적 락 재시도</b>: 예약 생성은 슬롯 정원 차감에서 동시 경합이 날 수 있어 재시도가 필요하다.
 * 재시도는 매 시도가 새 트랜잭션이어야 하므로 이 메서드는 <b>트랜잭션을 열지 않고</b>, 별도 빈
 * {@link ReservationBookingExecutor}의 {@code @Transactional} 메서드를 호출한다(self-invocation 회피).
 * 경합 판별은 프레임워크-프리 {@link OptimisticLockConflictException}(기존 슬롯 동시 update, infra
 * 어댑터가 spring-orm 예외를 번역)과 {@link DataIntegrityViolationException}(신규 슬롯 동시 insert 시
 * 유니크 충돌)으로 하고, 비즈니스 예외(SLOT_FULL/DUPLICATE/TERMS 등)는 재시도 없이 즉시 전파한다.
 *
 * <p>HTTP 경계에서 받은 {@code Long}은 이 계층에서 {@code ReservationId}·{@code MemberId}로 승격한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private static final int MAX_RETRY = 3;

    private final ReservationBookingExecutor reservationBookingExecutor;
    private final ReservationBookingService reservationBookingService;
    private final ReservationRepository reservationRepository;

    /**
     * 예약을 생성하고 생성된 예약 ID를 반환한다(비트랜잭션 — 낙관적 락 재시도 루프).
     *
     * <p>일시적 경합만 재시도하며, 재시도를 모두 소진하면 사실상 마감으로 간주해
     * {@code RESERVATION_SLOT_FULL}로 실패시킨다.
     */
    public Long createReservation(
        Long memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        MemberId memberIdVo = MemberId.of(memberId);

        for (int attempt = 0; ; attempt++) {
            try {
                ReservationId reservationId = reservationBookingExecutor.bookInNewTx(
                    memberIdVo,
                    shopId,
                    reservationDate,
                    reservationTime,
                    partySize,
                    request,
                    agreedRequiredTerms
                );
                return reservationId.value();
            } catch (OptimisticLockConflictException | DataIntegrityViolationException e) {
                log.warn("예약 생성 동시성 경합 재시도 {}/{}: shopId={}, date={}, time={}",
                    attempt + 1, MAX_RETRY, shopId, reservationDate, reservationTime);
                if (attempt == MAX_RETRY - 1) {
                    // 재시도 소진 = 사실상 마감으로 간주
                    throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
                }
            }
        }
    }

    /**
     * 점주 승인: PENDING → CONFIRMED. 슬롯 정원은 그대로 유지된다(예약이 살아 있으므로).
     * TODO(보안): Shop-owner 연결 후 점주 본인 검증(shop.getCeoId() == currentCeoId) 추가 필요.
     */
    @Transactional
    public void confirmReservation(Long id) {
        ReservationId reservationId = ReservationId.of(id);
        Reservation reservation = getReservation(reservationId);
        reservation.confirm();
        reservationRepository.save(reservation);
    }

    /**
     * 방문 완료: CONFIRMED → COMPLETED. 슬롯 정원은 그대로 유지된다(방문이 이뤄졌으므로).
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public void completeReservation(Long id) {
        ReservationId reservationId = ReservationId.of(id);
        Reservation reservation = getReservation(reservationId);
        reservation.complete();
        reservationRepository.save(reservation);
    }

    /**
     * 점주 거절: PENDING → REJECTED + 슬롯 정원 반납(원자 연산이라 도메인 서비스에 위임).
     * TODO(보안): 점주 본인 검증 필요.
     */
    @Transactional
    public void rejectReservation(Long id) {
        ReservationId reservationId = ReservationId.of(id);
        reservationBookingService.reject(reservationId);
    }

    /**
     * 회원 본인 취소: PENDING|CONFIRMED → CANCELED + 슬롯 정원 반납(원자 연산이라 도메인 서비스에 위임).
     */
    @Transactional
    public void cancelReservation(Long id, Long memberId) {
        ReservationId reservationId = ReservationId.of(id);
        MemberId memberIdVo = MemberId.of(memberId);
        reservationBookingService.cancel(reservationId, memberIdVo);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
