package com.tastyhouse.ceoapi.payment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteCommand;
import com.tastyhouse.application.payment.port.in.PaymentOnSiteCompleteUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;

@Tag(name = "Ceo Shop Payment", description = "점주 가게 결제 처리 API")
@RestController
@RequestMapping("/api/shops")
class ShopPaymentApiController {

    private final PaymentOnSiteCompleteUseCase paymentOnSiteCompleteUseCase;

    public ShopPaymentApiController(PaymentOnSiteCompleteUseCase paymentOnSiteCompleteUseCase) {
        this.paymentOnSiteCompleteUseCase = paymentOnSiteCompleteUseCase;
    }

    @Operation(summary = "현장결제 완료 처리", description = "점주가 매장에서 결제를 확인한 뒤, 로그인한 점주가 소유한 가게 주문의 현장결제(현장 현금·현장 카드)를 완료 처리합니다. 완료되면 주문이 확정되고 주문한 회원에게 결제 금액의 10%가 포인트로 적립됩니다.")
    @PostMapping("/v1/{id}/payments/{paymentId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeOnSitePayment(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long paymentId
    ) {
        PaymentOnSiteCompleteCommand command = PaymentOnSiteCompleteCommand.of(userDetails.getCeoId(), id, paymentId);
        paymentOnSiteCompleteUseCase.completeOnSitePayment(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
