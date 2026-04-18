package com.tastyhouse.webapi.review;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.review.request.CommentCreateRequest;
import com.tastyhouse.webapi.review.request.ReplyCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewType;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.review.response.BestReviewListItem;
import com.tastyhouse.webapi.review.response.CommentListResponse;
import com.tastyhouse.webapi.review.response.CommentResponse;
import com.tastyhouse.webapi.review.response.LatestReviewListItem;
import com.tastyhouse.webapi.review.response.ReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;
import com.tastyhouse.webapi.review.response.ReviewWriteInfoResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성 정보 조회", description = "주문 상품 ID로 리뷰 작성 페이지에 필요한 상품 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewWriteInfoResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "주문 상품을 찾을 수 없음")
    })
    @GetMapping("/v1/write/order-items/{orderItemId}")
    public ResponseEntity<CommonResponse<ReviewWriteInfoResponse>> getReviewWriteInfo(
            @Parameter(description = "주문 상품 ID", example = "1") @PathVariable Long orderItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReviewWriteInfoResponse response = reviewService.getReviewWriteInfo(orderItemId, userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "리뷰 등록", description = "리뷰를 등록합니다. orderItemId가 있으면 주문 기반 인증 리뷰, 없으면 일반 리뷰로 등록됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
        @ApiResponse(responseCode = "400", description = "이미 리뷰를 작성한 상품"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "상품 또는 주문 상품을 찾을 수 없음")
    })
    @PostMapping("/v1")
    public ResponseEntity<CommonResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReviewResponse response = reviewService.createReview(userDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "본인 리뷰가 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @PutMapping("/v1/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewResponse>> updateReview(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReviewResponse response = reviewService.updateReview(reviewId, userDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "본인 리뷰가 아님"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @DeleteMapping("/v1/{reviewId}")
    public ResponseEntity<CommonResponse<Void>> deleteReview(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @Operation(summary = "베스트 리뷰 목록 조회", description = "평점이 높은 순으로 정렬된 베스트 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/best")
    public ResponseEntity<CommonResponse<List<BestReviewListItem>>> getBestReviewList(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<BestReviewListItem> pageResult = reviewService.searchBestReviewList(pageRequest);
        CommonResponse<List<BestReviewListItem>> response = CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 리뷰 목록 조회", description = "최신 리뷰 목록을 페이징하여 조회합니다. type이 ALL이면 전체, FOLLOWING이면 팔로잉한 사용자의 리뷰만 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/latest")
    public ResponseEntity<CommonResponse<List<LatestReviewListItem>>> getLatestReviewList(
            @Valid @ModelAttribute PageRequest pageRequest,
            @Parameter(description = "조회 타입 (ALL: 전체, FOLLOWING: 팔로잉)", example = "ALL") @RequestParam(defaultValue = "ALL") ReviewType type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;
        PageResult<LatestReviewListItem> pageResult = reviewService.searchLatestReviewList(pageRequest, type, memberId);
        CommonResponse<List<LatestReviewListItem>> response = CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 리뷰 상세 정보를 조회합니다. 리뷰 태그 정보도 함께 조회됩니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewDetailResponse.class))), @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")})
    @GetMapping("/v1/{reviewId}")
    public ResponseEntity<CommonResponse<ReviewDetailResponse>> getReviewDetail(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        return reviewService.findReviewDetail(reviewId)
                .map(detail -> ResponseEntity.ok(CommonResponse.success(detail)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 상세 정보 조회 (상품 정보 포함)", description = "리뷰 ID로 리뷰 상세 정보와 연결된 상품 정보를 함께 조회합니다. 평점, 유저 정보, 작성일, 내용, 이미지, 태그 정보가 포함됩니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewProductResponse.class))), @ApiResponse(responseCode = "404", description = "리뷰 또는 상품을 찾을 수 없음")})
    @GetMapping("/v1/{reviewId}/product")
    public ResponseEntity<CommonResponse<ReviewProductResponse>> getReviewProduct(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        return reviewService.findReviewProduct(reviewId)
                .map(product -> ResponseEntity.ok(CommonResponse.success(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "리뷰 좋아요 여부 조회", description = "리뷰가 현재 사용자에 의해 좋아요되었는지 여부를 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공")})
    @GetMapping("/v1/{reviewId}/like")
    public ResponseEntity<CommonResponse<ReviewLikeStatusResponse>> isLiked(@PathVariable Long reviewId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReviewLikeStatusResponse liked;
        if (userDetails == null) {
            liked = ReviewLikeStatusResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            liked = reviewService.isLiked(reviewId, memberId);
        }
        return ResponseEntity.ok(CommonResponse.success(liked));
    }

    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 토글합니다. 이미 좋아요한 경우 취소되고, 아닌 경우 좋아요가 추가됩니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "좋아요 토글 성공", content = @Content(schema = @Schema(implementation = ReviewLikeResponse.class))), @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/{reviewId}/like")
    public ResponseEntity<CommonResponse<ReviewLikeResponse>> toggleReviewLike(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = reviewService.toggleReviewLike(reviewId, userDetails.getMemberId());
        return ResponseEntity.ok(CommonResponse.success(ReviewLikeResponse.from(liked)));
    }

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 등록 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))), @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/{reviewId}/comments")
    public ResponseEntity<CommonResponse<CommentResponse>> createComment(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId, @Valid @RequestBody CommentCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponse response = reviewService.createComment(reviewId, userDetails.getMemberId(), request.content());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "답글 등록", description = "댓글에 답글을 등록합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "답글 등록 성공", content = @Content(schema = @Schema(implementation = ReplyResponse.class))), @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
    @PostMapping("/v1/comments/{commentId}/replies")
    public ResponseEntity<CommonResponse<ReplyResponse>> createReply(@Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId, @Valid @RequestBody ReplyCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReplyResponse response = reviewService.createReply(commentId, userDetails.getMemberId(), request.replyToMemberId(), request.content());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "댓글 및 답글 조회", description = "리뷰의 모든 댓글과 답글을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentListResponse.class)))})
    @GetMapping("/v1/{reviewId}/comments")
    public ResponseEntity<CommonResponse<CommentListResponse>> getComments(@Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId) {
        CommentListResponse response = reviewService.searchCommentsWithReplies(reviewId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "특정 회원의 리뷰 목록 조회", description = "특정 회원이 작성한 리뷰 목록을 페이징하여 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/v1/members/{memberId}")
    public ResponseEntity<CommonResponse<List<MyReviewListItemResponse>>> getMemberReviews(
            @Parameter(description = "조회할 회원 ID", example = "1") @PathVariable Long memberId,
            @Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<MyReviewListItemResponse> pageResult = reviewService.findMemberReviews(memberId, pageRequest);
        CommonResponse<List<MyReviewListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(), pageResult.getCurrentPage(), pageResult.getPageSize(), pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }
}
