package com.tastyhouse.ceoapi.product.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductFeedbackSearchRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.request.ProductShopScopeRequest;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductFeedbackResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductFeedbackUnreadResponse;
import com.tastyhouse.ceoapplication.product.port.in.ProductFeedbackOwnerCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductFeedbackReadCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductFeedbackQueryUseCase;

/**
 * 점주 메뉴 정보 고객 의견 확인 API.
 *
 * <p><b>건별이 아니라 메뉴 × 유형 집계로 내려보낸다</b> — 점주가 알아야 하는 것은 "누가 언제 보냈는가"가
 * 아니라 "어떤 메뉴의 무엇이 몇 명에게 틀려 보이는가"다. 같은 지적이 수십 줄로 흩어지면 무엇을 고쳐야
 * 할지 판단할 수 없다.
 *
 * <p><b>제보자 정보는 어떤 응답에도 담기지 않는다.</b> 점주가 특정 손님을 식별하면 보복 우려가 있고,
 * 제보의 목적은 정보 수정이지 손님 응대가 아니다.
 *
 * <p>{@code shopId}를 경로가 아니라 query·바디로 받는다 — 경로에 가게 식별자가 없으면 소유권 검증을
 * 생략하기 쉽고, 이 저장소는 그 형태로 IDOR을 낸 전례가 있다.
 *
 * <p>역할 게이트({@code hasRole("CEO")})는 {@code SecurityConfig}가 담당하므로 별도 어노테이션이 없다.
 */
@Tag(name = "Ceo Product Feedback", description = "점주 메뉴 정보 고객 의견 API")
@RestController
@RequestMapping("/api/products")
public class ProductFeedbackApiController {

    private final ProductFeedbackQueryUseCase productFeedbackQueryService;
    private final ProductFeedbackOwnerCommandUseCase productFeedbackCommandUseCase;

    public ProductFeedbackApiController(
        ProductFeedbackQueryUseCase productFeedbackQueryService,
        ProductFeedbackOwnerCommandUseCase productFeedbackCommandUseCase
    ) {
        this.productFeedbackQueryService = productFeedbackQueryService;
        this.productFeedbackCommandUseCase = productFeedbackCommandUseCase;
    }

    @Operation(summary = "고객 의견 목록 조회",
        description = "지난 7일 동안 접수된 의견을 메뉴 × 유형으로 묶어 건수 많은 순으로 조회합니다. "
            + "조회 범위는 서버가 고정하며 요청으로 넓힐 수 없습니다. contents는 ETC 유형의 서술만 "
            + "최대 10건 담기고 그 외 유형은 빈 배열입니다. 제보자 정보는 내려주지 않습니다.")
    @GetMapping("/v1/feedbacks")
    public ResponseEntity<ApiResponse<List<ProductFeedbackResponse>>> getFeedbacks(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductFeedbackSearchRequest request
    ) {
        PageResult<ProductFeedbackSummaryResult> pageResult = productFeedbackQueryService.getFeedbacks(
            userDetails.getCeoId(), request.shopId(), request.page(), request.size()
        );
        PaginationResponse<ProductFeedbackResponse> result =
            PaginationResponse.from(pageResult.map(ProductFeedbackResponse::from));
        ApiResponse<List<ProductFeedbackResponse>> response = ApiResponse.success(
            result.content(), result.page(), result.size(), result.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "고객 의견 미확인 여부 조회",
        description = "확인하지 않은 의견이 있는지 조회합니다. 화면 아이콘의 빨간 점 표시에 사용합니다. "
            + "판정 범위도 지난 7일이라, 창 밖으로 밀려난 제보로 점이 켜져 목록을 열어도 끌 수 없는 "
            + "상태가 생기지 않습니다.")
    @GetMapping("/v1/feedbacks/unread")
    public ResponseEntity<ApiResponse<ProductFeedbackUnreadResponse>> getUnread(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @ModelAttribute ProductShopScopeRequest request
    ) {
        ProductFeedbackUnreadResponse response = ProductFeedbackUnreadResponse.from(productFeedbackQueryService.getUnread( userDetails.getCeoId(), request.shopId() ));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "고객 의견 확인 처리",
        description = "목록을 연 시점에 호출해 빨간 점을 끕니다. 확인 시각만 갱신되며 의견 자체는 "
            + "삭제되지 않습니다 — 반복 제보 추이가 근거 자료가 되기 때문입니다.")
    @PatchMapping("/v1/feedbacks/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ProductShopScopeRequest request
    ) {
        ProductFeedbackReadCommand command = request.toFeedbackReadCommand(userDetails.getCeoId());
        productFeedbackCommandUseCase.markRead(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
