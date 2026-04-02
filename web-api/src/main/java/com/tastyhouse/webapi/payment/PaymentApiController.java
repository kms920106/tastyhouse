package com.tastyhouse.webapi.payment;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.payment.request.PaymentCancelRequest;
import com.tastyhouse.webapi.payment.request.PaymentConfirmRequest;
import com.tastyhouse.webapi.payment.request.PaymentCreateRequest;
import com.tastyhouse.webapi.payment.request.RefundRequest;
import com.tastyhouse.webapi.payment.request.TossPaymentConfirmApiRequest;
import com.tastyhouse.webapi.payment.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.response.PaymentResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 API")
public class PaymentApiController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 생성", description = "주문에 대한 결제를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 생성 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)")
    })
    @PostMapping("/v1")
    public ResponseEntity<CommonResponse<PaymentResponse>> createPayment(
        @Valid @RequestBody PaymentCreateRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentResponse response = paymentService.createPayment(userDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "결제 승인 (PG 콜백)", description = "PG사로부터 결제 승인을 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/v1/confirm")
    public ResponseEntity<CommonResponse<PaymentResponse>> confirmPayment(
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "토스 결제 승인", description = "토스페이먼츠 결제를 승인합니다. 프론트엔드에서 /success 리다이렉트 후 호출합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 결제 승인 실패"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/toss/confirm")
    public ResponseEntity<CommonResponse<PaymentResponse>> confirmTossPayment(
        @Valid @RequestBody TossPaymentConfirmApiRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentResponse response = paymentService.confirmTossPayment(userDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "주문별 결제 조회", description = "주문에 대한 결제 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @GetMapping("/v1/order/{orderId}")
    public ResponseEntity<CommonResponse<PaymentResponse>> getPaymentByOrderId(
        @PathVariable Long orderId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(userDetails.getMemberId(), orderId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 결과", content = @Content(schema = @Schema(implementation = PaymentCancelResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/cancel")
    public ResponseEntity<CommonResponse<PaymentCancelResponse>> cancelPayment(
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentCancelRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentCancelResponse response = paymentService.cancelPayment(userDetails.getMemberId(), paymentId, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "현장결제 완료", description = "현장결제를 완료 처리합니다. 구매자가 직접 호출합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "현장결제 완료 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "완료할 수 없는 결제 상태"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/complete")
    public ResponseEntity<CommonResponse<PaymentResponse>> completeOnSitePayment(
        @PathVariable Long paymentId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentResponse response = paymentService.completeOnSitePayment(userDetails.getMemberId(), paymentId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "환불 요청", description = "결제에 대한 환불을 요청합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "환불 요청 성공", content = @Content(schema = @Schema(implementation = PaymentRefundResponse.class))),
        @ApiResponse(responseCode = "400", description = "환불할 수 없는 결제 상태"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/refund")
    public ResponseEntity<CommonResponse<PaymentRefundResponse>> requestRefund(
        @PathVariable Long paymentId,
        @Valid @RequestBody RefundRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentRefundResponse response = paymentService.requestRefund(userDetails.getMemberId(), paymentId, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
