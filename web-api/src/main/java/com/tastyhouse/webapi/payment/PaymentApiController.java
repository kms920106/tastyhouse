package com.tastyhouse.webapi.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.core.domain.payment.application.PaymentCommandService;
import com.tastyhouse.core.domain.payment.application.PaymentQueryService;
import com.tastyhouse.core.domain.payment.application.dto.command.CancelPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.ConfirmPaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.CreatePaymentCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.RequestRefundCommand;
import com.tastyhouse.core.domain.payment.application.dto.command.TossConfirmCommand;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentCancelResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentRefundResult;
import com.tastyhouse.core.domain.payment.application.dto.result.PaymentResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.payment.request.PaymentCancelRequest;
import com.tastyhouse.webapi.payment.request.PaymentConfirmRequest;
import com.tastyhouse.webapi.payment.request.PaymentCreateRequest;
import com.tastyhouse.webapi.payment.request.RefundRequest;
import com.tastyhouse.webapi.payment.request.TossPaymentConfirmApiRequest;
import com.tastyhouse.webapi.payment.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.response.PaymentResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 API")
public class PaymentApiController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @Operation(summary = "결제 생성", description = "주문에 대한 결제를 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 생성 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
        @Valid @RequestBody PaymentCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentResult result = paymentCommandService.createPayment(
            userDetails.getMemberId(),
            new CreatePaymentCommand(request.orderId(), request.paymentMethod())
        );
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(result)));
    }

    @Operation(summary = "결제 승인 (PG 콜백)", description = "PG사로부터 결제 승인을 처리합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentResult result = paymentCommandService.confirmPayment(
            new ConfirmPaymentCommand(
                request.paymentId(),
                request.pgProvider(),
                request.pgTid(),
                request.pgOrderId(),
                request.cardCompany(),
                request.cardNumber(),
                request.installmentMonths(),
                request.receiptUrl()
            )
        );
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(result)));
    }

    @Operation(summary = "토스 결제 승인", description = "토스페이먼츠 결제를 승인합니다. 프론트엔드에서 /success 리다이렉트 후 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 결제 승인 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/toss/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmTossPayment(
        @Valid @RequestBody TossPaymentConfirmApiRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentResult result = paymentCommandService.confirmTossPayment(
            userDetails.getMemberId(),
            new TossConfirmCommand(request.paymentKey(), request.pgOrderId(), request.amount())
        );
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(result)));
    }

    @Operation(summary = "주문별 결제 조회", description = "주문에 대한 결제 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @GetMapping("/v1/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
        @PathVariable Long orderId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentResult result = paymentQueryService.getPaymentByOrderId(userDetails.getMemberId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(result)));
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 결과", content = @Content(schema = @Schema(implementation = PaymentCancelResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancelPayment(
        @PathVariable Long paymentId,
        @Valid @RequestBody PaymentCancelRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentCancelResult result = paymentCommandService.cancelPayment(
            userDetails.getMemberId(), paymentId, new CancelPaymentCommand(request.cancelReason())
        );
        return ResponseEntity.ok(ApiResponse.success(PaymentCancelResponse.of(result)));
    }

    @Operation(summary = "현장결제 완료", description = "현장결제를 완료 처리합니다. 구매자가 직접 호출합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "현장결제 완료 성공", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "완료할 수 없는 결제 상태"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completeOnSitePayment(
        @PathVariable Long paymentId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentResult result = paymentCommandService.completeOnSitePayment(userDetails.getMemberId(), paymentId);
        return ResponseEntity.ok(ApiResponse.success(PaymentResponse.from(result)));
    }

    @Operation(summary = "환불 요청", description = "결제에 대한 환불을 요청합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "환불 요청 성공", content = @Content(schema = @Schema(implementation = PaymentRefundResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "환불할 수 없는 결제 상태"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 결제가 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    @PostMapping("/v1/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> requestRefund(
        @PathVariable Long paymentId,
        @Valid @RequestBody RefundRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentRefundResult result = paymentCommandService.requestRefund(
            userDetails.getMemberId(), paymentId, new RequestRefundCommand(request.refundAmount(), request.refundReason())
        );
        return ResponseEntity.ok(ApiResponse.success(PaymentRefundResponse.from(result)));
    }
}
