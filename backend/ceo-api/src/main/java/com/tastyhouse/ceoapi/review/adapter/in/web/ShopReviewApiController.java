package com.tastyhouse.ceoapi.review.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.review.adapter.in.web.request.ShopReviewSearchRequest;
import com.tastyhouse.ceoapi.review.adapter.in.web.request.ShopReviewSortTypeUpdateRequest;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ReviewBlindReasonCatalogResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewDetailResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewListItemResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewSortTypeResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewStatisticsResponse;
import com.tastyhouse.application.review.port.in.ShopReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ShopReviewSortTypeChangeCommand;
import com.tastyhouse.application.review.port.in.ShopReviewQueryUseCase;

@Tag(name = "Ceo Shop Review", description = "점주 리뷰 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopReviewApiController {

    private final ShopReviewQueryUseCase shopReviewQueryService;
    private final ShopReviewCommandUseCase shopReviewCommandUseCase;

    public ShopReviewApiController(
        ShopReviewQueryUseCase shopReviewQueryService,
        ShopReviewCommandUseCase shopReviewCommandUseCase
    ) {
        this.shopReviewQueryService = shopReviewQueryService;
        this.shopReviewCommandUseCase = shopReviewCommandUseCase;
    }

    @Operation(
        summary = "리뷰 목록 조회",
        description = "내 가게에 달린 리뷰를 조회합니다. 탭(전체·미답변·차단)·기간·별점·주문유형·사진 유무로 "
            + "필터할 수 있습니다. 차단된 리뷰도 목록에 포함되므로 고객 화면과 결과가 다를 수 있습니다."
    )
    @GetMapping("/v1/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ShopReviewListItemResponse>>> getReviews(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopReviewSearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ShopReviewListItemViewResult> pageResult = shopReviewQueryService.getReviews(
            userDetails.getCeoId(),
            id,
            request.tab(),
            request.startDate(),
            request.endDate(),
            request.rating(),
            request.orderMethod(),
            request.hasImage(),
            request.sortType(),
            pageRequest.page(),
            pageRequest.size()
        );
        PaginationResponse<ShopReviewListItemResponse> response =
            PaginationResponse.from(pageResult.map(ShopReviewListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            response.content(),
            response.page(),
            response.size(),
            response.totalElements()
        ));
    }

    @Operation(
        summary = "리뷰 통계 조회",
        description = "최근 6개월 기준 평균 별점·별점 분포·항목별 평균과 월별 추이를 조회합니다. "
            + "최근 180일간 리뷰가 1건도 없으면 hasData=false와 함께 빈 대시보드가 내려갑니다."
    )
    @GetMapping("/v1/{id}/reviews/statistics")
    public ResponseEntity<ApiResponse<ShopReviewStatisticsResponse>> getStatistics(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopReviewStatisticsResponse response =
            ShopReviewStatisticsResponse.from(shopReviewQueryService.getStatistics(userDetails.getCeoId(), id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "리뷰 정렬 설정 조회",
        description = "고객 앱 리뷰 목록에 적용되는 기본 정렬을 조회합니다. 한 번도 설정하지 않은 가게는 "
            + "기본값 최신순(LATEST)과 updatedAt=null이 내려갑니다."
    )
    @GetMapping("/v1/{id}/reviews/sort-type")
    public ResponseEntity<ApiResponse<ShopReviewSortTypeResponse>> getSortType(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopReviewSortTypeResponse response =
            ShopReviewSortTypeResponse.from(shopReviewQueryService.getSortType(userDetails.getCeoId(), id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "리뷰 정렬 설정 저장",
        description = "고객 앱 리뷰 목록의 기본 정렬을 저장합니다. 고객이 정렬을 직접 선택하면 그 선택이 "
            + "이 설정보다 우선합니다."
    )
    @PutMapping("/v1/{id}/reviews/sort-type")
    public ResponseEntity<ApiResponse<Void>> changeSortType(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopReviewSortTypeUpdateRequest request
    ) {
        ShopReviewSortTypeChangeCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopReviewCommandUseCase.changeSortType(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "리뷰 상세 조회",
        description = "리뷰 1건의 항목별 평점·태그·사장님 답변·게시중단 요청 이력을 함께 조회합니다."
    )
    @GetMapping("/v1/{id}/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ShopReviewDetailResponse>> getReviewDetail(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long reviewId
    ) {
        ShopReviewDetailResponse response =
            ShopReviewDetailResponse.from(shopReviewQueryService.getReviewDetail(userDetails.getCeoId(), id, reviewId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "게시중단 요청 사유 코드 목록 조회",
        description = "게시중단 요청 화면의 사유 선택지를 채우기 위한 정적 카탈로그입니다. 가게에 종속되지 "
            + "않으므로 소유권 검증이 없습니다."
    )
    @GetMapping("/v1/review-blind-reasons")
    public ResponseEntity<ApiResponse<List<ReviewBlindReasonCatalogResponse>>> getBlindReasons() {
        List<ReviewBlindReasonCatalogResponse> response = shopReviewQueryService.getBlindReasons().stream()
            .map(ReviewBlindReasonCatalogResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
