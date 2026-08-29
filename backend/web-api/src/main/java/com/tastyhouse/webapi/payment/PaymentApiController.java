package com.tastyhouse.webapi.payment;

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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.payment.request.PaymentCancelRequest;
import com.tastyhouse.webapi.payment.request.PaymentConfirmRequest;
import com.tastyhouse.webapi.payment.request.PaymentCreateRequest;
import com.tastyhouse.webapi.payment.request.RefundRequest;
import com.tastyhouse.webapi.payment.request.TossPaymentConfirmApiRequest;
import com.tastyhouse.webapi.payment.response.PaymentCancelResponse;
import com.tastyhouse.webapi.payment.response.PaymentRefundResponse;
import com.tastyhouse.webapi.payment.response.PaymentResponse;

/**
 * 회원 결제 API.
 *
 * <p>command(생성·승인·취소·현장완료·환불)와 조회를 CQRS로 분리한 두 서비스를 각각 주입한다
 * (공통 지침 패턴 2). command 서비스는 식별자만 돌려주므로, 커밋 이후 조회 서비스로 재조회해 응답을
 * 조립한다.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentApiController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    public PaymentApiController(PaymentCommandService paymentCommandService, PaymentQueryService paymentQueryService) {
        this.paymentCommandService = paymentCommandService;
        this.paymentQueryService = paymentQueryService;
    }

    @Operation(summary = "결제 생성", description = "주문에 대한 결제를 생성합니다. 생성된 결제 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createPayment(
        @Valid @RequestBody PaymentCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        Long paymentId = paymentCommandService.createPayment(memberId, request.orderId(), request.paymentMethod());
        return ResponseEntity.ok(ApiResponse.success(paymentId));
    }

    @Operation(summary = "결제 승인 (PG 콜백)", description = "PG사로부터 결제 승인을 처리합니다.")
    @PostMapping("/v1/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        Long paymentId = paymentCommandService.confirmPayment(
            request.paymentId(),
            request.pgProvider(),
            request.pgTid(),
            request.pgOrderId(),
            request.cardCompany(),
            request.cardNumber(),
            request.installmentMonths(),
            request.receiptUrl()
        );
        PaymentResponse response = paymentQueryService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "토스 결제 승인", description = "토스페이먼츠 결제를 승인합니다. 프론트엔드에서 /success 리다이렉트 후 호출합니다.")
    @PostMapping("/v1/toss/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmTossPayment(
        @Valid @RequestBody TossPaymentConfirmApiRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        Long paymentId = paymentCommandService.confirmTossPayment(
            memberId, request.paymentKey(), request.pgOrderId(), request.amount());
        PaymentResponse response = paymentQueryService.getPayment(memberId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문별 결제 조회", description = "주문에 대한 결제 정보를 조회합니다.")
    @GetMapping("/v1/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
        @PathVariable Long orderId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentResponse response = paymentQueryService.getPaymentByOrderId(userDetails.getMemberId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    @PostMapping("/v1/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancelPayment(
        @PathVariable Long id,
        @Valid @RequestBody PaymentCancelRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        PaymentCancelResponse response = paymentCommandService.cancelPayment(
            userDetails.getMemberId(), id, request.cancelReason());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "현장결제 완료", description = "현장결제를 완료 처리합니다. 구매자가 직접 호출합니다.")
    @PostMapping("/v1/{id}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completeOnSitePayment(
        @PathVariable Long id,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        Long paymentId = paymentCommandService.completeOnSitePayment(memberId, id);
        PaymentResponse response = paymentQueryService.getPayment(memberId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "환불 요청", description = "결제에 대한 환불을 요청합니다.")
    @PostMapping("/v1/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> requestRefund(
        @PathVariable Long id,
        @Valid @RequestBody RefundRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long refundId = paymentCommandService.requestRefund(
            userDetails.getMemberId(), id, request.refundAmount(), request.refundReason());
        PaymentRefundResponse response = paymentQueryService.getRefund(refundId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
