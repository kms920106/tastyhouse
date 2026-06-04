package com.tastyhouse.webapi.reservation;

import com.tastyhouse.core.domain.reservation.application.ReservationCommandService;
import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.command.CreateReservationCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.reservation.request.ReservationCreateRequest;
import com.tastyhouse.webapi.reservation.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.response.ReservationResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 API")
public class ReservationApiController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationService reservationService;

    @Operation(summary = "예약 생성", description = "가게 시간 슬롯에 예약을 신청합니다. (PENDING)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "예약 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 / 약관 미동의 / 과거 일시"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "슬롯 마감 / 중복 예약")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
        @Valid @RequestBody ReservationCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        CreateReservationCommand command = new CreateReservationCommand(
            request.shopId(),
            request.reservationDate(),
            request.reservationTime(),
            request.partySize(),
            request.request(),
            request.agreedRequiredTerms()
        );
        ReservationResult result = reservationCommandService.create(userDetails.getMemberId(), command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ReservationResponse.from(result)));
    }

    @Operation(summary = "내 예약 목록 조회", description = "로그인한 회원의 예약 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
        @CurrentUser CustomUserDetails userDetails
    ) {
        List<ReservationResponse> responses = reservationQueryService.findMyReservations(userDetails.getMemberId()).stream()
            .map(ReservationResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "예약 완료 상세 조회", description = "본인의 예약 상세를 조회합니다.")
    @GetMapping("/v1/{reservationId}/complete")
    public ResponseEntity<ApiResponse<ReservationCompleteDetailResponse>> getDetail(
        @PathVariable Long reservationId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReservationCompleteDetailResponse response = reservationService.getDetail(userDetails.getMemberId(), reservationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 상세 조회", description = "본인의 예약 단건 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 상세 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 예약이 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @GetMapping("/v1/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationDetail(
        @PathVariable Long reservationId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReservationDetailResponse response = reservationService.getReservationDetail(userDetails.getMemberId(), reservationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "예약 취소", description = "본인의 예약을 취소합니다. (PENDING|CONFIRMED -> CANCELED)")
    @PatchMapping("/v1/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(
        @PathVariable Long reservationId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReservationResult result = reservationCommandService.cancel(userDetails.getMemberId(), reservationId);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.from(result)));
    }

    @Operation(summary = "예약 승인(점주)", description = "점주가 예약을 승인합니다. (PENDING -> CONFIRMED)")
    @PatchMapping("/v1/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable Long reservationId) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationResult result = reservationCommandService.confirm(reservationId);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.from(result)));
    }

    @Operation(summary = "예약 거절(점주)", description = "점주가 예약을 거절합니다. (PENDING -> REJECTED, 정원 반납)")
    @PatchMapping("/v1/{reservationId}/reject")
    public ResponseEntity<ApiResponse<ReservationResponse>> reject(@PathVariable Long reservationId) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationResult result = reservationCommandService.reject(reservationId);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.from(result)));
    }

    @Operation(summary = "방문 완료(점주)", description = "점주가 방문 완료 처리합니다. (CONFIRMED -> COMPLETED)")
    @PatchMapping("/v1/{reservationId}/complete")
    public ResponseEntity<ApiResponse<ReservationResponse>> complete(@PathVariable Long reservationId) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        ReservationResult result = reservationCommandService.complete(reservationId);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.from(result)));
    }

    @Operation(summary = "가게별 예약 목록 조회(점주)", description = "특정 가게의 예약 목록을 조회합니다.")
    @GetMapping("/v1/shops/{shopId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getShopReservations(@PathVariable Long shopId) {
        // TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요
        List<ReservationResponse> responses = reservationQueryService.findShopReservations(shopId).stream()
            .map(ReservationResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
