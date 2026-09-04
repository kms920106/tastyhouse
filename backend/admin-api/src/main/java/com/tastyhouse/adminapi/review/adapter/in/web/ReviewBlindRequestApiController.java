package com.tastyhouse.adminapi.review.adapter.in.web;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestApproveCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestManagementCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestRejectCommand;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.request.ReviewBlindRequestRejectRequest;
import com.tastyhouse.adminapi.review.adapter.in.web.request.ReviewBlindRequestSearchRequest;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestDetailResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewBlindRequestListItemResponse;
import com.tastyhouse.application.review.port.out.ReviewBlindRequestListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestQueryUseCase;

@Tag(name = "Review Blind Request Admin", description = "리뷰 게시중단 요청 심사 API")
@RestController
@RequestMapping("/api/reviews")
public class ReviewBlindRequestApiController {

    private final ReviewBlindRequestQueryUseCase reviewBlindRequestQueryUseCase;
    private final ReviewBlindRequestManagementCommandUseCase reviewBlindRequestCommandUseCase;

    public ReviewBlindRequestApiController(
        ReviewBlindRequestQueryUseCase reviewBlindRequestQueryUseCase,
        ReviewBlindRequestManagementCommandUseCase reviewBlindRequestCommandUseCase
    ) {
        this.reviewBlindRequestQueryUseCase = reviewBlindRequestQueryUseCase;
        this.reviewBlindRequestCommandUseCase = reviewBlindRequestCommandUseCase;
    }

    @Operation(summary = "게시중단 요청 목록 조회", description = "리뷰 게시중단 요청 목록을 상점/상태/사유/기간으로 페이징 조회합니다.")
    @GetMapping("/v1/blind-requests")
    public ResponseEntity<ApiResponse<List<ReviewBlindRequestListItemResponse>>> getBlindRequests(
        @Valid @ModelAttribute ReviewBlindRequestSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<ReviewBlindRequestListItemResult> pageResult = reviewBlindRequestQueryUseCase.getBlindRequests(
            search.shopId(),
            search.status(),
            search.reason(),
            search.startDate(),
            search.endDate(),
            pageRequest.page(),
            pageRequest.size()
        );
        PaginationResponse<ReviewBlindRequestListItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(ReviewBlindRequestListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "게시중단 요청 상세 조회", description = "리뷰 게시중단 요청 심사 상세를 조회합니다.")
    @GetMapping("/v1/blind-requests/{id}")
    public ResponseEntity<ApiResponse<ReviewBlindRequestDetailResponse>> getBlindRequest(@PathVariable Long id) {
        ReviewBlindRequestDetailResponse response = ReviewBlindRequestDetailResponse.from(reviewBlindRequestQueryUseCase.getBlindRequest(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "게시중단 요청 승인", description = "리뷰 게시중단 요청을 승인하고 대상 리뷰를 숨깁니다.")
    @PutMapping("/v1/blind-requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveBlindRequest(@PathVariable Long id) {
        ReviewBlindRequestApproveCommand command = ReviewBlindRequestApproveCommand.of(id);
        reviewBlindRequestCommandUseCase.approveBlindRequest(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "게시중단 요청 반려", description = "리뷰 게시중단 요청을 반려합니다.")
    @PutMapping("/v1/blind-requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectBlindRequest(
        @PathVariable Long id,
        @Valid @RequestBody ReviewBlindRequestRejectRequest request
    ) {
        ReviewBlindRequestRejectCommand command = request.toCommand(id);
        reviewBlindRequestCommandUseCase.rejectBlindRequest(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
