package com.tastyhouse.webapi.reservation.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.reservation.adapter.in.web.request.ReservationCreateRequest;
import com.tastyhouse.webapi.reservation.adapter.in.web.request.ReservationSearchRequest;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationSlotAvailabilityResponse;
import com.tastyhouse.webapplication.reservation.port.in.ReservationCancelCommand;
import com.tastyhouse.webapplication.reservation.port.in.ReservationCommandUseCase;
import com.tastyhouse.webapplication.reservation.port.in.ReservationCompleteCommand;
import com.tastyhouse.webapplication.reservation.port.in.ReservationConfirmCommand;
import com.tastyhouse.webapplication.reservation.port.in.ReservationCreateCommand;
import com.tastyhouse.webapplication.reservation.port.in.ReservationQueryUseCase;
import com.tastyhouse.webapplication.reservation.port.in.ReservationRejectCommand;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation", description = "예약 API")
public class ReservationApiController {

    private final ReservationCommandUseCase reservationCommandUseCase;
    private final ReservationQueryUseCase reservationQueryService;

    public ReservationApiController(
        ReservationCommandUseCase reservationCommandUseCase,
        ReservationQueryUseCase reservationQueryService
    ) {
        this.reservationCommandUseCase = reservationCommandUseCase;
        this.reservationQueryService = reservationQueryService;
    }

    @Operation(summary = "슬롯 가용성 조회", description = "가게의 특정 날짜 슬롯별 잔여/가용 정보를 조회합니다. 로그인 필수 — 내 예약 슬롯은 available=false로 반환.")
    @GetMapping("/v1/availability")
    public ResponseEntity<ApiResponse<ReservationSlotAvailabilityResponse>> getAvailability(
        @Valid @ModelAttribute ReservationSearchRequest search,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReservationSlotAvailabilityResponse response = ReservationSlotAvailabilityResponse.from(
            reservationQueryService.getAvailability(search.shopId(), search.date(), userDetails.getMemberId())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 생성", description = "가게 시간 슬롯에 예약을 신청합니다. (PENDING) 생성된 예약 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> create(
        @Valid @RequestBody ReservationCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReservationCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long reservationId = reservationCommandUseCase.createReservation(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(reservationId));
    }

    @Operation(summary = "내 예약 목록 조회", description = "로그인한 회원의 예약 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
        @CurrentUser MemberUserDetails userDetails
    ) {
        List<ReservationResponse> responses = reservationQueryService.getMyReservations(userDetails.getMemberId()).stream()
            .map(ReservationResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "예약 완료 상세 조회", description = "본인의 예약 상세를 조회합니다.")
    @GetMapping("/v1/{id}/complete")
    public ResponseEntity<ApiResponse<ReservationCompleteDetailResponse>> getDetail(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReservationCompleteDetailResponse response = ReservationCompleteDetailResponse.from(
            reservationQueryService.getCompleteDetail(userDetails.getMemberId(), id)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 상세 조회", description = "본인의 예약 단건 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationDetail(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReservationDetailResponse response = ReservationDetailResponse.from(
            reservationQueryService.getReservationDetail(userDetails.getMemberId(), id)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 취소", description = "본인의 예약을 취소합니다. (PENDING|CONFIRMED -> CANCELED)")
    @PatchMapping("/v1/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReservationCancelCommand command = ReservationCancelCommand.of(userDetails.getMemberId(), id);
        reservationCommandUseCase.cancelReservation(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "예약 승인(점주)", description = "점주가 예약을 승인합니다. (PENDING -> CONFIRMED)")
    @PatchMapping("/v1/{id}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable Long id) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationConfirmCommand command = ReservationConfirmCommand.of(id);
        reservationCommandUseCase.confirmReservation(command);
        ReservationResponse response = ReservationResponse.from(reservationQueryService.getReservation(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 거절(점주)", description = "점주가 예약을 거절합니다. (PENDING -> REJECTED, 정원 반납)")
    @PatchMapping("/v1/{id}/reject")
    public ResponseEntity<ApiResponse<ReservationResponse>> reject(@PathVariable Long id) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationRejectCommand command = ReservationRejectCommand.of(id);
        reservationCommandUseCase.rejectReservation(command);
        ReservationResponse response = ReservationResponse.from(reservationQueryService.getReservation(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "방문 완료(점주)", description = "점주가 방문 완료 처리합니다. (CONFIRMED -> COMPLETED)")
    @PatchMapping("/v1/{id}/complete")
    public ResponseEntity<ApiResponse<ReservationResponse>> complete(@PathVariable Long id) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationCompleteCommand command = ReservationCompleteCommand.of(id);
        reservationCommandUseCase.completeReservation(command);
        ReservationResponse response = ReservationResponse.from(reservationQueryService.getReservation(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게별 예약 목록 조회(점주)", description = "특정 가게의 예약 목록을 조회합니다.")
    @GetMapping("/v1/shops/{shopId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getShopReservations(@PathVariable Long shopId) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        List<ReservationResponse> responses = reservationQueryService.getShopReservations(shopId).stream()
            .map(ReservationResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
