package com.tastyhouse.webapi.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.reservation.application.ReservationCommandService;
import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.command.ReservationCreateCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.reservation.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationResponse;
import com.tastyhouse.webapi.reservation.response.ReservationSlot;
import com.tastyhouse.webapi.reservation.response.ReservationSlotAvailabilityResponse;

/**
 * 예약 조회 응답 가공 전용 web-api 서비스.
 * core-module이 반환하는 raw 파일 경로를 {@link FileService}로 접근 가능한 URL로 변환해
 * 다른 가게 이미지 응답과 동일한 형식(완전한 URL)을 보장한다.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final MemberQueryService memberQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public ReservationSlotAvailabilityResponse getAvailability(Long shopId, LocalDate date, Long memberId) {
        DailySlotAvailabilityResult result = reservationQueryService.findSlotAvailability(shopId, date, MemberId.of(memberId));
        return toSlotAvailabilityResponse(result);
    }

    public ReservationResponse create(
        Long memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        MemberId memberIdVo = MemberId.of(memberId);
        ReservationCreateCommand command = ReservationCreateCommand.of(
            shopId, reservationDate, reservationTime, partySize, request, agreedRequiredTerms);
        ReservationResult result = reservationCommandService.create(memberIdVo, command);
        return toReservationResponse(result);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long memberId) {
        return reservationQueryService.findMyReservations(MemberId.of(memberId)).stream()
            .map(this::toReservationResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ReservationCompleteDetailResponse getDetail(Long memberId, Long reservationId) {
        ReservationResult result = reservationQueryService.findDetail(MemberId.of(memberId), ReservationId.of(reservationId));
        return ReservationCompleteDetailResponse.from(
            result.id().value(),
            result.shopName(),
            fileService.getUrlByPath(result.shopImageUrl()),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize()
        );
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservationDetail(Long memberId, Long reservationId) {
        ReservationResult result = reservationQueryService.findDetail(MemberId.of(memberId), ReservationId.of(reservationId));
        Member reserver = memberQueryService.getById(result.memberId());
        String phoneNumber = reserver.getPhoneNumber() != null ? reserver.getPhoneNumber().getValue() : null;
        return ReservationDetailResponse.from(
            result.id().value(),
            result.shopId(),
            result.shopName(),
            fileService.getUrlByPath(result.shopImageUrl()),
            result.shopRoadAddress(),
            result.shopLotAddress(),
            result.memberId().value(),
            reserver.getFullName(),
            phoneNumber,
            reserver.getUsername(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize(),
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }

    public void cancel(Long reservationId, Long memberId) {
        ReservationId id = ReservationId.of(reservationId);
        MemberId memberIdVo = MemberId.of(memberId);
        reservationCommandService.cancel(id, memberIdVo);
    }

    public ReservationResponse confirm(Long reservationId) {
        ReservationId id = ReservationId.of(reservationId);
        ReservationResult result = reservationCommandService.confirm(id);
        return toReservationResponse(result);
    }

    public ReservationResponse reject(Long reservationId) {
        ReservationId id = ReservationId.of(reservationId);
        ReservationResult result = reservationCommandService.reject(id);
        return toReservationResponse(result);
    }

    public ReservationResponse complete(Long reservationId) {
        ReservationId id = ReservationId.of(reservationId);
        ReservationResult result = reservationCommandService.complete(id);
        return toReservationResponse(result);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getShopReservations(Long shopId) {
        return reservationQueryService.findShopReservations(shopId).stream()
            .map(this::toReservationResponse)
            .toList();
    }

    private ReservationResponse toReservationResponse(ReservationResult result) {
        return ReservationResponse.from(
            result.id().value(),
            result.shopId(),
            result.shopName(),
            result.memberId().value(),
            result.reservationDate(),
            result.reservationTime(),
            result.partySize(),
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }

    private ReservationSlotAvailabilityResponse toSlotAvailabilityResponse(DailySlotAvailabilityResult result) {
        List<ReservationSlot> slots = result.slots().stream()
            .map(s -> new ReservationSlot(s.time(), s.remaining(), s.available()))
            .toList();
        return ReservationSlotAvailabilityResponse.from(result.date(), result.hasMyReservation(), slots);
    }
}
