package com.tastyhouse.webapi.payment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.payment.port.in.PaymentByOrderQueryUseCase;
import com.tastyhouse.application.payment.port.in.PaymentCancelCommand;
import com.tastyhouse.application.payment.port.in.PaymentCancelUseCase;
import com.tastyhouse.application.payment.port.in.PaymentCreateCommand;
import com.tastyhouse.application.payment.port.in.PaymentCreateUseCase;
import com.tastyhouse.application.payment.port.in.PaymentDetailQueryUseCase;
import com.tastyhouse.application.payment.port.in.PaymentRefundDetailQueryUseCase;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestCommand;
import com.tastyhouse.application.payment.port.in.PaymentRefundRequestUseCase;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmCommand;
import com.tastyhouse.application.payment.port.in.PgPaymentConfirmUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.payment.adapter.in.web.request.PaymentCancelRequest;
import com.tastyhouse.webapi.payment.adapter.in.web.request.PaymentCreateRequest;
import com.tastyhouse.webapi.payment.adapter.in.web.request.RefundRequest;
import com.tastyhouse.webapi.payment.adapter.in.web.request.TossPaymentConfirmApiRequest;
import com.tastyhouse.webapi.payment.adapter.in.web.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.adapter.in.web.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.adapter.in.web.response.PaymentResponse;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 API")
class PaymentApiController {

    private final PaymentCreateUseCase paymentCreateUseCase;
    private final PgPaymentConfirmUseCase pgPaymentConfirmUseCase;
    private final PaymentCancelUseCase paymentCancelUseCase;
    private final PaymentRefundRequestUseCase paymentRefundRequestUseCase;
    private final PaymentDetailQueryUseCase paymentDetailQueryUseCase;
    private final PaymentByOrderQueryUseCase paymentByOrderQueryUseCase;
    private final PaymentRefundDetailQueryUseCase paymentRefundDetailQueryUseCase;

    public PaymentApiController(
        PaymentCreateUseCase paymentCreateUseCase,
        PgPaymentConfirmUseCase pgPaymentConfirmUseCase,
        PaymentCancelUseCase paymentCancelUseCase,
        PaymentRefundRequestUseCase paymentRefundRequestUseCase,
        PaymentDetailQueryUseCase paymentDetailQueryUseCase,
        PaymentByOrderQueryUseCase paymentByOrderQueryUseCase,
        PaymentRefundDetailQueryUseCase paymentRefundDetailQueryUseCase
    ) {
        this.paymentCreateUseCase = paymentCreateUseCase;
        this.pgPaymentConfirmUseCase = pgPaymentConfirmUseCase;
        this.paymentCancelUseCase = paymentCancelUseCase;
        this.paymentRefundRequestUseCase = paymentRefundRequestUseCase;
        this.paymentDetailQueryUseCase = paymentDetailQueryUseCase;
        this.paymentByOrderQueryUseCase = paymentByOrderQueryUseCase;
        this.paymentRefundDetailQueryUseCase = paymentRefundDetailQueryUseCase;
    }

    @Operation(summary = "결제 생성", description = "주문에 대한 결제를 생성합니다. 생성된 결제 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createPayment(
        @Valid @RequestBody PaymentCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaymentCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long paymentId = paymentCreateUseCase.createPayment(command);
        return ResponseEntity.ok(ApiResponse.success(paymentId));
    }

    @Operation(summary = "토스 결제 승인", description = "토스페이먼츠 결제를 승인합니다. 프론트엔드에서 /success 리다이렉트 후 호출합니다.")
    @PostMapping("/v1/toss/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmTossPayment(
        @Valid @RequestBody TossPaymentConfirmApiRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        PgPaymentConfirmCommand command = request.toCommand(memberId);
        Long paymentId = pgPaymentConfirmUseCase.confirmPgPayment(command);
        PaymentResponse response = PaymentResponse.from(paymentDetailQueryUseCase.getPayment(memberId, paymentId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문별 결제 조회", description = "주문에 대한 결제 정보를 조회합니다.")
    @GetMapping("/v1/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
        @PathVariable Long orderId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaymentResponse response = PaymentResponse.from(paymentByOrderQueryUseCase.getPaymentByOrderId(userDetails.getMemberId(), orderId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @PostMapping("/v1/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancelPayment(
        @PathVariable Long id,
        @Valid @RequestBody PaymentCancelRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaymentCancelCommand command = request.toCommand(userDetails.getMemberId(), id);
        PaymentCancelResponse response = PaymentCancelResponse.from(paymentCancelUseCase.cancelPayment(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "환불 요청", description = "결제에 대한 환불을 요청합니다.")
    @PostMapping("/v1/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> requestRefund(
        @PathVariable Long id,
        @Valid @RequestBody RefundRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaymentRefundRequestCommand command = request.toCommand(userDetails.getMemberId(), id);
        Long refundId = paymentRefundRequestUseCase.requestRefund(command);
        PaymentRefundResponse response = PaymentRefundResponse.from(paymentRefundDetailQueryUseCase.getRefund(refundId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
