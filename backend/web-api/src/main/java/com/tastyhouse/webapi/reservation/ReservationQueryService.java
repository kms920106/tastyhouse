package com.tastyhouse.webapi.reservation;

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

import com.tastyhouse.domain.reservation.service.SlotPolicy;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.infrastructure.reservation.query.ReservationDetailResult;
import com.tastyhouse.infrastructure.reservation.query.ReservationQueryDao;
import com.tastyhouse.infrastructure.reservation.query.ReservationResult;
import com.tastyhouse.infrastructure.reservation.query.SlotOccupancyResult;
import com.tastyhouse.webapi.reservation.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationResponse;
import com.tastyhouse.webapi.reservation.response.ReservationSlot;
import com.tastyhouse.webapi.reservation.response.ReservationSlotAvailabilityResponse;

/**
 * 예약 조회 서비스(web).
 *
 * <p>infra read 어댑터({@link ReservationQueryDao})만 주입해 조회하고 Response를 조립한다(private 매퍼).
 * 가게 썸네일은 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 응답에 전달한다.
 *
 * <p>본인 예약 여부 검증은 조회 경로에서도 필요하다 — 도메인 모델을 거치지 않는 read 경로이므로
 * 조회 결과의 {@code memberId}를 요청자와 직접 비교해 남의 예약 조회를 차단한다.
 */
@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    /**
     * 슬롯 과거 여부 판정 기준 시간대 — 서비스 운영 지역(한국) 고정.
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationQueryDao reservationQueryDao;

    public ReservationQueryService(ReservationQueryDao reservationQueryDao) {
        this.reservationQueryDao = reservationQueryDao;
    }

    /**
     * 특정 가게·날짜의 슬롯별 가용성. 슬롯 행이 없는 시간대는 예약 0건이므로 정원 전체가 잔여다.
     */
    public ReservationSlotAvailabilityResponse getAvailability(Long shopId, LocalDate date, Long memberId) {
        Map<LocalTime, Integer> remainingByTime = reservationQueryDao.findSlotOccupancies(shopId, date).stream()
            .collect(Collectors.toMap(SlotOccupancyResult::slotTime, SlotOccupancyResult::remaining));

        // 회원+가게+날짜당 차단 예약은 최대 1건. 존재 여부로 그 날짜 전체 슬롯 비활성화를 판단한다.
        boolean hasMyReservation = reservationQueryDao.existsBlockingReservation(memberId, shopId, date);

        LocalDateTime now = LocalDateTime.now(KST);

        List<ReservationSlot> slots = SlotPolicy.allSlots().stream()
            .map(time -> {
                int remaining = remainingByTime.getOrDefault(time, SlotPolicy.CAPACITY_PER_SLOT);
                boolean notPast = LocalDateTime.of(date, time).isAfter(now);
                // 이미 그 날짜에 예약이 있으면(가게+날짜 1건 제약) 모든 슬롯을 비활성화한다.
                boolean available = remaining > 0 && notPast && !hasMyReservation;
                return new ReservationSlot(time, remaining, available);
            })
            .toList();

        return ReservationSlotAvailabilityResponse.from(date, hasMyReservation, slots);
    }

    /**
     * 내 예약 목록.
     */
    public List<ReservationResponse> getMyReservations(Long memberId) {
        return reservationQueryDao.findReservationsByMemberId(memberId).stream()
            .map(this::toReservationResponse)
            .toList();
    }

    /**
     * 특정 가게의 예약 목록(점주 화면).
     * TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요.
     */
    public List<ReservationResponse> getShopReservations(Long shopId) {
        return reservationQueryDao.findReservationsByShopId(shopId).stream()
            .map(this::toReservationResponse)
            .toList();
    }

    /**
     * 단건 조회 후 Response 조립에 쓰는 공통 경로 — 예약 생성·상태전이 직후 응답에도 재사용한다.
     */
    public ReservationResponse getReservation(Long id) {
        ReservationResult result = reservationQueryDao.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        return toReservationResponse(result);
    }

    /**
     * 예약 완료 화면 상세 — 본인 예약만 조회할 수 있다.
     */
    public ReservationCompleteDetailResponse getCompleteDetail(Long memberId, Long id) {
        ReservationResult result = reservationQueryDao.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return ReservationCompleteDetailResponse.from(
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
    public ReservationDetailResponse getReservationDetail(Long memberId, Long id) {
        ReservationDetailResult result = reservationQueryDao.findReservationDetailById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return ReservationDetailResponse.from(
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

    private ReservationResponse toReservationResponse(ReservationResult result) {
        return ReservationResponse.from(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.memberId(),
            result.reservationDate(),
            result.reservationTime(),
            result.partySize(),
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }
}
