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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.review.adapter.in.web.request.CommentCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReplyCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewSearchRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewBestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewCommentListResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLikeResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewWriteInfoResponse;
import com.tastyhouse.application.review.port.in.ReviewCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleCommand;
import com.tastyhouse.application.review.port.in.ReviewQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewUpdateCommand;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewApiController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryUseCase reviewQueryService;

    public ReviewApiController(
        ReviewCommandUseCase reviewCommandUseCase,
        ReviewQueryUseCase reviewQueryService
    ) {
        this.reviewCommandUseCase = reviewCommandUseCase;
        this.reviewQueryService = reviewQueryService;
    }

    @Operation(summary = "리뷰 작성 정보 조회", description = "주문 상품 ID로 리뷰 작성 페이지에 필요한 상품 정보를 조회합니다.")
    @GetMapping("/v1/write/order-items/{orderProductId}")
    public ResponseEntity<ApiResponse<ReviewWriteInfoResponse>> getReviewWriteInfo(
        @Parameter(description = "주문 상품 ID", example = "1") @PathVariable Long orderProductId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewWriteInfoResponse response = ReviewWriteInfoResponse.from(
            reviewQueryService.getReviewWriteInfo(orderProductId, memberIdOrNull(userDetails))
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
    @PutMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @Valid @RequestBody ReviewUpdateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewUpdateCommand command = request.toCommand(userDetails.getMemberId(), reviewId);
        Long updatedReviewId = reviewCommandUseCase.updateReview(command);
        ReviewResponse response = ReviewResponse.from(
            reviewQueryService.getReviewSubmitResult(updatedReviewId, userDetails.getMemberId())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewDeleteCommand command = ReviewDeleteCommand.of(userDetails.getMemberId(), reviewId);
        reviewCommandUseCase.deleteReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "베스트 리뷰 목록 조회", description = "평점이 높은 순으로 정렬된 베스트 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<ReviewBestListItemResponse>>> getBestReviewList(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<ReviewBestListItemResponse> pageResponse = PaginationResponse.from(
            reviewQueryService.searchBestReviewList(pageRequest.page(), pageRequest.size())
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
            reviewQueryService.searchLatestReviewList(pageRequest.page(), pageRequest.size(), search.type(), memberId)
                .map(ReviewLatestListItemResponse::from)
        );
        ApiResponse<List<ReviewLatestListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다. 리뷰 태그 정보도 함께 조회됩니다.")
    @GetMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReviewDetail(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        return reviewQueryService.findReviewDetail(reviewId, memberIdOrNull(userDetails))
                .map(detail -> ResponseEntity.ok(ApiResponse.success(ReviewDetailResponse.from(detail))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 상세 정보 조회 (상품 정보 포함)", description = "리뷰 ID로 리뷰 상세 정보와 연결된 상품 정보를 함께 조회합니다. 평점, 유저 정보, 작성일, 내용, 이미지, 태그 정보가 포함됩니다.")
    @GetMapping("/v1/{reviewId}/product")
    public ResponseEntity<ApiResponse<ReviewProductResponse>> getReviewProduct(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        return reviewQueryService.findReviewProduct(reviewId, memberIdOrNull(userDetails))
                .map(product -> ResponseEntity.ok(ApiResponse.success(ReviewProductResponse.from(product))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 좋아요 여부 조회", description = "리뷰가 현재 사용자에 의해 좋아요되었는지 여부를 조회합니다.")
    @GetMapping("/v1/{reviewId}/like")
    public ResponseEntity<ApiResponse<ReviewLikeStatusResponse>> isLiked(
        @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewLikeStatusResponse liked;
        if (userDetails == null) {
            liked = ReviewLikeStatusResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            liked = ReviewLikeStatusResponse.from(reviewQueryService.isLiked(reviewId, memberId));
        }
        return ResponseEntity.ok(ApiResponse.success(liked));
    }

    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 토글합니다. 이미 좋아요한 경우 취소되고, 아닌 경우 좋아요가 추가됩니다.")
    @PostMapping("/v1/{reviewId}/like")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> toggleReviewLike(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        reviewQueryService.requireVisibleReview(reviewId, userDetails.getMemberId());
        ReviewLikeToggleCommand command = ReviewLikeToggleCommand.of(userDetails.getMemberId(), reviewId);
        boolean liked = reviewCommandUseCase.toggleReviewLike(command);
        return ResponseEntity.ok(ApiResponse.success(ReviewLikeResponse.from(liked)));
    }

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다. 생성된 댓글 ID를 반환합니다.")
    @PostMapping("/v1/{reviewId}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @Valid @RequestBody CommentCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        reviewQueryService.requireVisibleReview(reviewId, userDetails.getMemberId());
        ReviewCommentCreateCommand command = request.toCommand(userDetails.getMemberId(), reviewId);
        Long commentId = reviewCommandUseCase.createComment(command);
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }

    @Operation(summary = "답글 등록", description = "댓글에 답글을 등록합니다. 생성된 답글 ID를 반환합니다.")
    @PostMapping("/v1/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Long>> createReply(
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
        @Valid @RequestBody ReplyCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        Long parentReviewId = reviewCommandUseCase.findReviewIdOfComment(commentId);
        reviewQueryService.requireVisibleReview(parentReviewId, userDetails.getMemberId());
        ReviewReplyCreateCommand command = request.toCommand(userDetails.getMemberId(), commentId);
        Long replyId = reviewCommandUseCase.createReply(command);
        return ResponseEntity.ok(ApiResponse.success(replyId));
    }

    @Operation(summary = "댓글 및 답글 조회", description = "리뷰의 모든 댓글과 답글을 조회합니다.")
    @GetMapping("/v1/{reviewId}/comments")
    public ResponseEntity<ApiResponse<ReviewCommentListResponse>> getComments(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewCommentListResponse response = ReviewCommentListResponse.from(
            reviewQueryService.searchCommentsWithReplies(reviewId, memberIdOrNull(userDetails))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "특정 회원의 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/members/{memberId}")
    public ResponseEntity<ApiResponse<List<ReviewMemberListItemResponse>>> getMemberReviews(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ReviewMemberListItemResponse> pageResponse = PaginationResponse.from(
            reviewQueryService.findMemberReviews(memberId, pageRequest.page(), pageRequest.size())
                .map(ReviewMemberListItemResponse::from)
        );
        ApiResponse<List<ReviewMemberListItemResponse>> response = ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 선택적 인증 — {@code /api/reviews/**}는 permitAll이라 비로그인 접근이 가능하므로
     * {@code @CurrentUser}가 {@code null}로 들어올 수 있다. 사장님만보기 리뷰의 본인 판정에 쓰인다
     * ({@code ShopApiController}의 동명 헬퍼와 같은 형태).
     */
    private Long memberIdOrNull(MemberUserDetails userDetails) {
        return userDetails == null ? null : userDetails.getMemberId();
    }
}
