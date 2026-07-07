package com.tastyhouse.webapi.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.reservation.application.ReservationCommandService;
import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.command.ReservationCreateCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.reservation.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationResponse;
import com.tastyhouse.webapi.reservation.response.SlotAvailabilityResponse;

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
    public SlotAvailabilityResponse getAvailability(Long shopId, LocalDate date, Long memberId) {
        DailySlotAvailabilityResult result = reservationQueryService.findSlotAvailability(shopId, date, memberId);
        return SlotAvailabilityResponse.from(result);
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
        ReservationCreateCommand command = ReservationCreateCommand.of(
            shopId, reservationDate, reservationTime, partySize, request, agreedRequiredTerms);
        ReservationResult result = reservationCommandService.create(memberId, command);
        return ReservationResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long memberId) {
        return reservationQueryService.findMyReservations(memberId).stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ReservationCompleteDetailResponse getDetail(Long memberId, Long reservationId) {
        ReservationResult result = reservationQueryService.findDetail(memberId, ReservationId.of(reservationId));
        return ReservationCompleteDetailResponse.from(result, fileService.getUrlByPath(result.shopImageUrl()));
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponse getReservationDetail(Long memberId, Long reservationId) {
        ReservationResult result = reservationQueryService.findDetail(memberId, ReservationId.of(reservationId));
        Member reserver = memberQueryService.getById(result.memberId());
        String phoneNumber = reserver.getPhoneNumber() != null ? reserver.getPhoneNumber().getValue() : null;
        return ReservationDetailResponse.from(
            result,
            fileService.getUrlByPath(result.shopImageUrl()),
            reserver.getFullName(),
            phoneNumber,
            reserver.getUsername()
        );
    }

    public void cancel(Long reservationId, Long memberId) {
        reservationCommandService.cancel(ReservationId.of(reservationId), memberId);
    }

    public ReservationResponse confirm(Long reservationId) {
        ReservationResult result = reservationCommandService.confirm(ReservationId.of(reservationId));
        return ReservationResponse.from(result);
    }

    public ReservationResponse reject(Long reservationId) {
        ReservationResult result = reservationCommandService.reject(ReservationId.of(reservationId));
        return ReservationResponse.from(result);
    }

    public ReservationResponse complete(Long reservationId) {
        ReservationResult result = reservationCommandService.complete(ReservationId.of(reservationId));
        return ReservationResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getShopReservations(Long shopId) {
        return reservationQueryService.findShopReservations(shopId).stream()
            .map(ReservationResponse::from)
            .toList();
    }
}
