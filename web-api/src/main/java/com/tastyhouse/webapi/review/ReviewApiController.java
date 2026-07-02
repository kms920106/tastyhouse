package com.tastyhouse.webapi.review;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.core.domain.review.domain.model.ReviewType;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.review.request.CommentCreateRequest;
import com.tastyhouse.webapi.review.request.ReplyCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.response.BestReviewListItemResponse;
import com.tastyhouse.webapi.review.response.CommentListResponse;
import com.tastyhouse.webapi.review.response.CommentResponse;
import com.tastyhouse.webapi.review.response.LatestReviewListItemResponse;
import com.tastyhouse.webapi.review.response.MemberReviewListItemResponse;
import com.tastyhouse.webapi.review.response.ReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;
import com.tastyhouse.webapi.review.response.ReviewWriteInfoResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성 정보 조회", description = "주문 상품 ID로 리뷰 작성 페이지에 필요한 상품 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewWriteInfoResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 상품을 찾을 수 없음")
    })
    @GetMapping("/v1/write/order-items/{orderProductId}")
    public ResponseEntity<ApiResponse<ReviewWriteInfoResponse>> getReviewWriteInfo(
        @Parameter(description = "주문 상품 ID", example = "1") @PathVariable Long orderProductId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReviewWriteInfoResponse response = reviewService.getReviewWriteInfo(orderProductId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다. orderProductId가 있으면 주문 기반 인증 리뷰, 없으면 일반 리뷰로 등록됩니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 리뷰를 작성한 상품"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 주문 상품을 찾을 수 없음")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
        @Valid @RequestBody ReviewCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReviewResponse response = reviewService.createReview(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 리뷰가 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @PutMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @Valid @RequestBody ReviewUpdateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 리뷰가 아님"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @DeleteMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "베스트 리뷰 목록 조회", description = "평점이 높은 순으로 정렬된 베스트 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<BestReviewListItemResponse>>> getBestReviewList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<BestReviewListItemResponse> pageResult = reviewService.searchBestReviewList(pageRequest.page(), pageRequest.size());
        ApiResponse<List<BestReviewListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 리뷰 목록 조회", description = "최신 리뷰 목록을 페이징하여 조회합니다. type이 ALL이면 전체, FOLLOWING이면 팔로잉한 사용자의 리뷰만 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<LatestReviewListItemResponse>>> getLatestReviewList(
        @Valid @ModelAttribute PageRequest pageRequest,
        @Parameter(description = "조회 타입 (ALL: 전체, FOLLOWING: 팔로잉)", example = "ALL") @RequestParam(defaultValue = "ALL") ReviewType type,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        PageResult<LatestReviewListItemResponse> pageResult = reviewService.searchLatestReviewList(pageRequest.page(), pageRequest.size(), type, memberId);
        ApiResponse<List<LatestReviewListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다. 리뷰 태그 정보도 함께 조회됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewDetailResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")})
    @GetMapping("/v1/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResponse>> getReviewDetail(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        return reviewService.findReviewDetail(reviewId)
                .map(detail -> ResponseEntity.ok(ApiResponse.success(detail)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 상세 정보 조회 (상품 정보 포함)", description = "리뷰 ID로 리뷰 상세 정보와 연결된 상품 정보를 함께 조회합니다. 평점, 유저 정보, 작성일, 내용, 이미지, 태그 정보가 포함됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewProductResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰 또는 상품을 찾을 수 없음")})
    @GetMapping("/v1/{reviewId}/product")
    public ResponseEntity<ApiResponse<ReviewProductResponse>> getReviewProduct(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        return reviewService.findReviewProduct(reviewId)
                .map(product -> ResponseEntity.ok(ApiResponse.success(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 좋아요 여부 조회", description = "리뷰가 현재 사용자에 의해 좋아요되었는지 여부를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")})
    @GetMapping("/v1/{reviewId}/like")
    public ResponseEntity<ApiResponse<ReviewLikeStatusResponse>> isLiked(
        @PathVariable Long reviewId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReviewLikeStatusResponse liked;
        if (userDetails == null) {
            liked = ReviewLikeStatusResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            liked = reviewService.isLiked(reviewId, memberId);
        }
        return ResponseEntity.ok(ApiResponse.success(liked));
    }

    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 토글합니다. 이미 좋아요한 경우 취소되고, 아닌 경우 좋아요가 추가됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 토글 성공", content = @Content(schema = @Schema(implementation = ReviewLikeResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/{reviewId}/like")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> toggleReviewLike(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        boolean liked = reviewService.toggleReviewLike(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(ReviewLikeResponse.from(liked)));
    }

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 등록 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/{reviewId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
        @Valid @RequestBody CommentCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        CommentResponse response = reviewService.createComment(reviewId, userDetails.getMemberId(), request.content());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "답글 등록", description = "댓글에 답글을 등록합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답글 등록 성공", content = @Content(schema = @Schema(implementation = ReplyResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
        @Valid @RequestBody ReplyCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ReplyResponse response = reviewService.createReply(commentId, userDetails.getMemberId(), request.replyToMemberId(), request.content());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "댓글 및 답글 조회", description = "리뷰의 모든 댓글과 답글을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentListResponse.class)))})
    @GetMapping("/v1/{reviewId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        CommentListResponse response = reviewService.searchCommentsWithReplies(reviewId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "특정 회원의 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/members/{memberId}")
    public ResponseEntity<ApiResponse<List<MemberReviewListItemResponse>>> getMemberReviews(
        @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<MemberReviewListItemResponse> pageResult = reviewService.findMemberReviews(memberId, pageRequest.page(), pageRequest.size());
        ApiResponse<List<MemberReviewListItemResponse>> response = ApiResponse.success(
            pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements()
        );
        return ResponseEntity.ok(response);
    }
}
