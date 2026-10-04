package com.tastyhouse.webapi.review.adapter.in.web;

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

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.review.port.in.ReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewUpdateCommand;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewSearchRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewBestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewWriteInfoResponse;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review", description = "리뷰 관리 API")
class ReviewApiController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryUseCase reviewQueryUseCase;

    public ReviewApiController(
        ReviewCommandUseCase reviewCommandUseCase,
        ReviewQueryUseCase reviewQueryUseCase
    ) {
        this.reviewCommandUseCase = reviewCommandUseCase;
        this.reviewQueryUseCase = reviewQueryUseCase;
    }

    @Operation(summary = "리뷰 작성 정보 조회", description = "주문 상품 ID로 리뷰 작성 페이지에 필요한 상품 정보를 조회합니다.")
    @GetMapping("/v1/write/order-items/{orderProductId}")
    public ResponseEntity<ApiResponse<ReviewWriteInfoResponse>> getReviewWriteInfo(
        @Parameter(description = "주문 상품 ID", example = "1") @PathVariable Long orderProductId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewWriteInfoResponse response = ReviewWriteInfoResponse.from(
            reviewQueryUseCase.getReviewWriteInfo(orderProductId, memberIdOrNull(userDetails))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다. orderProductId가 있으면 주문 기반 인증 리뷰, 없으면 일반 리뷰로 등록됩니다. 생성된 리뷰 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createReview(
        @Valid @RequestBody ReviewCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long reviewId = reviewCommandUseCase.createReview(command);
        return ResponseEntity.ok(ApiResponse.success(reviewId));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody ReviewUpdateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewUpdateCommand command = request.toCommand(userDetails.getMemberId(), id);
        Long updatedReviewId = reviewCommandUseCase.updateReview(command);
        ReviewResponse response = ReviewResponse.from(
            reviewQueryUseCase.getReviewSubmitResult(updatedReviewId, userDetails.getMemberId())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewDeleteCommand command = ReviewDeleteCommand.of(userDetails.getMemberId(), id);
        reviewCommandUseCase.deleteReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "베스트 리뷰 목록 조회", description = "평점이 높은 순으로 정렬된 베스트 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<ReviewBestListItemResponse>>> getBestReviewList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<ReviewBestListItemResponse> pageResponse = PaginationResponse.from(
            reviewQueryUseCase.searchBestReviewList(pageRequest.page(), pageRequest.size())
                .map(ReviewBestListItemResponse::from)
        );
        ApiResponse<List<ReviewBestListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 리뷰 목록 조회", description = "최신 리뷰 목록을 페이징하여 조회합니다. type이 ALL이면 전체, FOLLOWING이면 팔로잉한 사용자의 리뷰만 조회합니다.")
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<ReviewLatestListItemResponse>>> getLatestReviewList(
        @Valid @ModelAttribute PageRequest pageRequest,
        @Valid @ModelAttribute ReviewSearchRequest search,
        @CurrentUser MemberUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        PaginationResponse<ReviewLatestListItemResponse> pageResponse = PaginationResponse.from(
            reviewQueryUseCase.searchLatestReviewList(pageRequest.page(), pageRequest.size(), search.type(), memberId)
                .map(ReviewLatestListItemResponse::from)
        );
        ApiResponse<List<ReviewLatestListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다. 리뷰 태그 정보도 함께 조회됩니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReviewDetail(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        return reviewQueryUseCase.findReviewDetail(id, memberIdOrNull(userDetails))
                .map(detail -> ResponseEntity.ok(ApiResponse.success(ReviewDetailResponse.from(detail))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 상세 정보 조회 (상품 정보 포함)", description = "리뷰 ID로 리뷰 상세 정보와 연결된 상품 정보를 함께 조회합니다. 평점, 유저 정보, 작성일, 내용, 이미지, 태그 정보가 포함됩니다.")
    @GetMapping("/v1/{id}/product")
    public ResponseEntity<ApiResponse<ReviewProductResponse>> getReviewProduct(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        return reviewQueryUseCase.findReviewProduct(id, memberIdOrNull(userDetails))
                .map(product -> ResponseEntity.ok(ApiResponse.success(ReviewProductResponse.from(product))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "특정 회원의 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/members/{memberId}")
    public ResponseEntity<ApiResponse<List<ReviewMemberListItemResponse>>> getMemberReviews(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ReviewMemberListItemResponse> pageResponse = PaginationResponse.from(
            reviewQueryUseCase.findMemberReviews(memberId, pageRequest.page(), pageRequest.size())
                .map(ReviewMemberListItemResponse::from)
        );
        ApiResponse<List<ReviewMemberListItemResponse>> response = ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    private Long memberIdOrNull(MemberUserDetails userDetails) {
        return userDetails == null ? null : userDetails.getMemberId();
    }
}
