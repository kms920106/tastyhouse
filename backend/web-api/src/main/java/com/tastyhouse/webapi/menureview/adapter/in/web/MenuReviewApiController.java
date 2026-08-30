package com.tastyhouse.webapi.menureview.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.auth.security.CustomUserDetails;
import com.tastyhouse.webapi.menureview.adapter.in.web.request.MenuReviewCreateRequest;
import com.tastyhouse.webapi.menureview.adapter.in.web.request.MenuReviewUpdateRequest;
import com.tastyhouse.webapplication.menureview.response.MenuReviewListItemResponse;
import com.tastyhouse.webapplication.menureview.response.MenuReviewWritableItemResponse;
import com.tastyhouse.webapplication.menureview.port.in.MenuReviewCommandUseCase;
import com.tastyhouse.webapplication.menureview.port.in.MenuReviewCreateCommand;
import com.tastyhouse.webapplication.menureview.port.in.MenuReviewDeleteCommand;
import com.tastyhouse.webapplication.menureview.port.in.MenuReviewQueryUseCase;
import com.tastyhouse.webapplication.menureview.port.in.MenuReviewUpdateCommand;
import com.tastyhouse.webapi.security.CurrentUser;

/**
 * 메뉴 평가 API — 매장 리뷰({@code /api/reviews})와 <b>독립된 축</b>이다.
 *
 * <p>매장 리뷰를 쓰지 않아도 메뉴 평가만 남길 수 있고 그 반대도 가능하다. 그래서 두 API 사이에 호출
 * 순서 제약이 없다.
 */
@RestController
@RequestMapping("/api/menu-reviews")
@Tag(name = "MenuReview", description = "메뉴 평가 API")
public class MenuReviewApiController {

    private final MenuReviewCommandUseCase menuReviewCommandUseCase;
    private final MenuReviewQueryUseCase menuReviewQueryService;

    public MenuReviewApiController(
        MenuReviewCommandUseCase menuReviewCommandUseCase,
        MenuReviewQueryUseCase menuReviewQueryService
    ) {
        this.menuReviewCommandUseCase = menuReviewCommandUseCase;
        this.menuReviewQueryService = menuReviewQueryService;
    }

    @Operation(
        summary = "평가 가능 메뉴 목록 조회",
        description = "주문의 메뉴 중 평가 가능한 것(주류·사이드 등 평가 제외 메뉴 제외)을 조회합니다. "
            + "이미 평가한 메뉴도 기존 평점·코멘트와 함께 내려주므로 폼 초기값으로 사용할 수 있습니다."
    )
    @GetMapping("/v1/writable/orders/{orderId}")
    public ResponseEntity<ApiResponse<List<MenuReviewWritableItemResponse>>> getWritableItems(
        @Parameter(description = "주문 ID", example = "100") @PathVariable Long orderId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        List<MenuReviewWritableItemResponse> response =
            menuReviewQueryService.findWritableItems(orderId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "메뉴 평가 등록",
        description = "주문 항목 하나에 메뉴 평가를 등록합니다. 매장 리뷰를 쓰지 않았어도 등록됩니다. "
            + "생성된 메뉴 평가 ID를 반환합니다."
    )
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createMenuReview(
        @Valid @RequestBody MenuReviewCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        MenuReviewCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long menuReviewId = menuReviewCommandUseCase.createMenuReview(command);
        return ResponseEntity.ok(ApiResponse.success(menuReviewId));
    }

    @Operation(summary = "메뉴 평가 수정", description = "본인이 작성한 메뉴 평가의 평점·코멘트를 수정합니다. 작성 근거인 주문 항목은 바꿀 수 없습니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMenuReview(
        @Parameter(description = "메뉴 평가 ID", example = "77") @PathVariable Long id,
        @Valid @RequestBody MenuReviewUpdateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        MenuReviewUpdateCommand command = request.toCommand(userDetails.getMemberId(), id);
        menuReviewCommandUseCase.updateMenuReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "메뉴 평가 삭제", description = "본인이 작성한 메뉴 평가를 삭제합니다. 삭제하면 그 주문 항목에 다시 평가를 남길 수 있습니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuReview(
        @Parameter(description = "메뉴 평가 ID", example = "77") @PathVariable Long id,
        @CurrentUser CustomUserDetails userDetails
    ) {
        MenuReviewDeleteCommand command = MenuReviewDeleteCommand.of(userDetails.getMemberId(), id);
        menuReviewCommandUseCase.deleteMenuReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품별 메뉴 평가 목록 조회", description = "상품에 달린 메뉴 평가를 최신순으로 페이징 조회합니다(공개 조회). 댓글·좋아요·사장님답변은 없습니다.")
    @GetMapping("/v1/products/{productId}")
    public ResponseEntity<ApiResponse<List<MenuReviewListItemResponse>>> getMenuReviewsByProduct(
        @Parameter(description = "상품 ID", example = "1") @PathVariable Long productId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<MenuReviewListItemResponse> pageResponse =
            menuReviewQueryService.findByProductId(productId, pageRequest.page(), pageRequest.size());
        ApiResponse<List<MenuReviewListItemResponse>> response = ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        );
        return ResponseEntity.ok(response);
    }
}
