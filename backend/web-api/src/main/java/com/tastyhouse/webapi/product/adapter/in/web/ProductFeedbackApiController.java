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
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.product.application.port.in.ProductFeedbackCommandUseCase;
import com.tastyhouse.webapi.product.application.port.in.ProductFeedbackCreateCommand;
import com.tastyhouse.webapi.product.adapter.in.web.request.ProductFeedbackCreateRequest;
import com.tastyhouse.webapi.security.CurrentUser;

/**
 * 손님의 메뉴 정보 의견 제보 API.
 *
 * <p><b>리뷰와 다른 리소스다</b> — 리뷰는 "음식이 어땠는지"이고 이것은 "등록된 정보가 틀렸다"는 제보다.
 * 별점에 반영되지 않으며, 점주에게는 건별이 아니라 주간 집계로 전달된다.
 *
 * <p><b>로그인이 필수다.</b> 익명 제보를 열면 경쟁 가게의 반복 허위 제보를 막을 수 없다. 다만 저장한
 * 회원 식별자는 중복 방지에만 쓰고 <b>점주에게는 노출하지 않는다</b> — 점주가 제보자를 식별하면 보복
 * 우려가 있고, 제보의 목적은 정보 수정이지 손님 응대가 아니다.
 */
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
        @CurrentUser CustomUserDetails userDetails
    ) {
        ProductFeedbackCreateCommand command = request.toCommand(userDetails.getMemberId(), id);
        Long feedbackId = productFeedbackCommandUseCase.submitFeedback(command);
        return ResponseEntity.ok(ApiResponse.success(feedbackId));
    }
}
