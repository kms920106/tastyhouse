package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentListView;
import com.tastyhouse.application.review.port.out.ReviewDetailView;
import com.tastyhouse.application.review.port.out.ReviewProductView;
import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;
import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;

/**
 * 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b>에서 반환 타입이 Response에서 읽기 계약으로 바뀌었다 — Response record가 web-api로
 * 승격됐으므로 표현 조립은 컨트롤러가 담당한다. 페이징은 {@code PageResult}로 반환하고 컨트롤러가
 * {@code PaginationResponse.from(...)}으로 감싼다.
 */
@WebApp
public interface ReviewQueryUseCase {

    PageResult<BestReviewListItemResult> searchBestReviewList(int page, int size);

    PageResult<LatestReviewListItemResult> searchLatestReviewList(int page, int size, String type, Long memberId);

    Optional<ReviewDetailView> findReviewDetail(Long reviewId, Long viewerMemberId);

    ReviewSubmitResultView getReviewSubmitResult(Long reviewId, Long authorMemberId);

    /** 리뷰 좋아요 여부. */
    boolean isLiked(Long reviewId, Long memberId);

    ReviewCommentListView searchCommentsWithReplies(Long reviewId, Long viewerMemberId);

    Optional<ReviewProductView> findReviewProduct(Long reviewId, Long viewerMemberId);

    ReviewWriteInfoView getReviewWriteInfo(Long orderProductId, Long memberId);

    PageResult<MyReviewListItemResult> findMemberReviews(Long memberId, int page, int size);

    void requireVisibleReview(Long reviewId, Long viewerMemberId);
}
