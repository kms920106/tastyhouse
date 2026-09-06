package com.tastyhouse.webapi.product.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.product.port.in.ProductFeedbackCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductFeedbackCreateCommand;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductFeedbackCreateRequest;
import com.tastyhouse.webapi.security.CurrentUser;

@Tag(name = "Product Feedback", description = "메뉴 정보 고객 의견 API")
@RestController
@RequestMapping("/api/products")
public class ProductFeedbackApiController {
    private final ProductFeedbackCommandUseCase productFeedbackCommandUseCase;

    public ProductFeedbackApiController(ProductFeedbackCommandUseCase productFeedbackCommandUseCase) {
        this.productFeedbackCommandUseCase = productFeedbackCommandUseCase;
    }

    @Operation(summary = "메뉴 정보 의견 보내기",
        description = "메뉴에 등록된 정보(가격·이미지·구성·품절 여부)가 실제와 다르다는 의견을 보냅니다. "
            + "유형이 ETC이면 내용이 필수이며 500자를 넘을 수 없습니다. 같은 메뉴에 같은 유형으로는 "
            + "7일 내 다시 보낼 수 없습니다(PRODUCT_FEEDBACK_ALREADY_SUBMITTED).")
    @PostMapping("/v1/{id}/feedbacks")
    public ResponseEntity<ApiResponse<Long>> createFeedback(
        @Parameter(description = "메뉴 ID", example = "100") @PathVariable Long id,
        @Valid @RequestBody ProductFeedbackCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ProductFeedbackCreateCommand command = request.toCommand(userDetails.getMemberId(), id);
        Long feedbackId = productFeedbackCommandUseCase.submitFeedback(command);
        return ResponseEntity.ok(ApiResponse.success(feedbackId));
    }
}
