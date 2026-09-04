package com.tastyhouse.webapplication.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.reservation.service.SlotPolicy;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.out.ReservationDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.reservation.port.out.SlotOccupancyResult;
import com.tastyhouse.webapplication.reservation.port.in.ReservationQueryUseCase;
import com.tastyhouse.webapplication.reservation.port.out.ReservationCompleteDetailResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationDetailViewResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationSlotAvailabilityResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationSlotResult;

/**
 * 예약 조회 서비스(web).
 *
 * <p>읽기 포트({@link ReservationQueryPort})만 주입해 조회하고 읽기 계약을 조립한다. 가게 썸네일은
 * DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 담아 넘긴다. 표현 계약
 * ({@code Reservation*Response}) 조립은 web-api가 담당한다(챕터 10).
 *
 * <p>본인 예약 여부 검증은 조회 경로에서도 필요하다 — 도메인 모델을 거치지 않는 read 경로이므로
 * 조회 결과의 {@code memberId}를 요청자와 직접 비교해 남의 예약 조회를 차단한다.
 */
@Service
@Transactional(readOnly = true)
public class ReservationQueryService implements ReservationQueryUseCase {

    /**
     * 슬롯 과거 여부 판정 기준 시간대 — 서비스 운영 지역(한국) 고정.
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationQueryPort reservationQueryPort;

    public ReservationQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    /**
     * 특정 가게·날짜의 슬롯별 가용성. 슬롯 행이 없는 시간대는 예약 0건이므로 정원 전체가 잔여다.
     */
    @Override
    public ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId) {
        Map<LocalTime, Integer> remainingByTime = reservationQueryPort.findSlotOccupancies(shopId, date).stream()
            .collect(Collectors.toMap(SlotOccupancyResult::slotTime, SlotOccupancyResult::remaining));

        // 회원+가게+날짜당 차단 예약은 최대 1건. 존재 여부로 그 날짜 전체 슬롯 비활성화를 판단한다.
        boolean hasMyReservation = reservationQueryPort.existsBlockingReservation(memberId, shopId, date);

        LocalDateTime now = LocalDateTime.now(KST);

        List<ReservationSlotResult> slots = SlotPolicy.allSlots().stream()
            .map(time -> {
                int remaining = remainingByTime.getOrDefault(time, SlotPolicy.CAPACITY_PER_SLOT);
                boolean notPast = LocalDateTime.of(date, time).isAfter(now);
                // 이미 그 날짜에 예약이 있으면(가게+날짜 1건 제약) 모든 슬롯을 비활성화한다.
                boolean available = remaining > 0 && notPast && !hasMyReservation;
                return new ReservationSlotResult(time, remaining, available);
            })
            .toList();

        return new ReservationSlotAvailabilityResult(date, hasMyReservation, slots);
    }

    /**
     * 내 예약 목록.
     */
    @Override
    public List<ReservationResult> getMyReservations(Long memberId) {
        return reservationQueryPort.findReservationsByMemberId(memberId);
    }

    /**
     * 특정 가게의 예약 목록(점주 화면).
     * TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요.
     */
    @Override
    public List<ReservationResult> getShopReservations(Long shopId) {
        return reservationQueryPort.findReservationsByShopId(shopId);
    }

    /**
     * 단건 조회 공통 경로 — 예약 생성·상태전이 직후 응답에도 재사용한다.
     */
    @Override
    public ReservationResult getReservation(Long id) {
        return reservationQueryPort.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약 완료 화면 상세 — 본인 예약만 조회할 수 있다.
     */
    @Override
    public ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id) {
        ReservationResult result = reservationQueryPort.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return new ReservationCompleteDetailResult(
            result.id(),
            result.shopName(),
            result.shopImageUrl(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize()
        );
    }

    /**
     * 예약 상세(예약자 정보 포함) — 본인 예약만 조회할 수 있다.
     */
    @Override
    public ReservationDetailViewResult getReservationDetail(Long memberId, Long id) {
        ReservationDetailResult result = reservationQueryPort.findReservationDetailById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return new ReservationDetailViewResult(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.shopImageUrl(),
            result.shopRoadAddress(),
            result.shopLotAddress(),
            result.memberId(),
            result.reserverName(),
            result.reserverPhoneNumber(),
            result.reserverEmail(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize(),
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }

    /**
     * 예약자 본인 검증. 도메인 모델을 거치지 않는 read 경로이므로 조회 결과의 예약자 ID를 직접 비교한다
     * (도메인 모델의 {@code Reservation#validateOwnership}과 같은 규칙·같은 에러코드).
     */
    private void validateOwnership(Long ownerId, Long requesterId) {
        if (!Objects.equals(ownerId, requesterId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }
}
