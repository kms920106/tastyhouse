package com.tastyhouse.webapi.review.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.review.application.port.in.ReviewBlindConsentCommand;
import com.tastyhouse.webapi.review.application.port.in.ReviewBlindConsentCommandUseCase;
import com.tastyhouse.webapi.review.application.port.in.ReviewBlindRejectCommand;
import com.tastyhouse.webapi.review.application.service.ReviewBlindConsentQueryService;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewBlindNoticeResponse;

/**
 * 게시중단된 내 리뷰의 삭제 동의·거부 API.
 *
 * <p>이미 비대해진 {@code ReviewApiController}에 얹지 않고 별도 컨트롤러로 두는 이유는, 이 두 경로가
 * "게시중단 생애주기"라는 다른 관심사이고 인가 규칙(작성자 본인 + 게시중단 상태)도 리뷰 CRUD와 다르기
 * 때문이다.
 *
 * <p><b>응답 코드가 ceo 경로와 갈린다</b> — 대상이 이미 게시중단된 비공개 리뷰이므로 타인 리뷰 접근을
 * 403이 아니라 404({@code REVIEW_NOT_FOUND})로 응답해 존재 자체를 숨긴다. 판단 근거는 도메인 서비스의
 * Javadoc에 있다.
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Blind Consent", description = "게시중단 리뷰 삭제 동의 API")
public class ReviewBlindConsentApiController {

    private final ReviewBlindConsentCommandUseCase reviewBlindConsentCommandUseCase;
    private final ReviewBlindConsentQueryService reviewBlindConsentQueryService;

    public ReviewBlindConsentApiController(
        ReviewBlindConsentCommandUseCase reviewBlindConsentCommandUseCase,
        ReviewBlindConsentQueryService reviewBlindConsentQueryService
    ) {
        this.reviewBlindConsentCommandUseCase = reviewBlindConsentCommandUseCase;
        this.reviewBlindConsentQueryService = reviewBlindConsentQueryService;
    }

    @Operation(
        summary = "게시중단 리뷰 안내 조회",
        description = "게시중단된 내 리뷰의 내용·사유·재노출 예정일을 조회합니다. 삭제 동의 여부를 판단하는 "
            + "화면에서 사용합니다. 일반 리뷰 상세 조회는 게시중단된 리뷰를 404로 처리하므로 이 경로를 씁니다. "
            + "리뷰가 없거나 게시중단 상태가 아니거나 타인의 리뷰이면 404를 반환합니다."
    )
    @GetMapping("/v1/{reviewId}/blind")
    public ResponseEntity<ApiResponse<ReviewBlindNoticeResponse>> getBlindNotice(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId
    ) {
        ReviewBlindNoticeResponse response = reviewBlindConsentQueryService.getBlindNotice(
            reviewId, userDetails.getMemberId()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "게시중단 리뷰 삭제 동의",
        description = "게시중단된 내 리뷰의 삭제에 동의합니다. 리뷰가 사진·태그와 함께 즉시 삭제되며 "
            + "게시중단 요청은 삭제 처리로 종결됩니다. 게시중단 상태가 아니면 409를 반환합니다."
    )
    @PostMapping("/v1/{reviewId}/blind/consent")
    public ResponseEntity<ApiResponse<Void>> consentToDelete(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId
    ) {
        ReviewBlindConsentCommand command = ReviewBlindConsentCommand.of(userDetails.getMemberId(), reviewId);
        reviewBlindConsentCommandUseCase.consent(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "게시중단 리뷰 삭제 거부",
        description = "게시중단된 내 리뷰의 삭제를 거부합니다. 아무 상태도 바뀌지 않으며 게시중단 30일이 "
            + "지나면 리뷰가 자동으로 다시 노출됩니다. 게시중단 상태가 아니면 409를 반환합니다."
    )
    @PostMapping("/v1/{reviewId}/blind/reject")
    public ResponseEntity<ApiResponse<Void>> rejectDeletion(
        @CurrentUser CustomUserDetails userDetails,
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId
    ) {
        ReviewBlindRejectCommand command = ReviewBlindRejectCommand.of(userDetails.getMemberId(), reviewId);
        reviewBlindConsentCommandUseCase.reject(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
