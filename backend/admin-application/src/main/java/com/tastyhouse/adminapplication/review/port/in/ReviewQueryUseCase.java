package com.tastyhouse.adminapplication.review.port.in;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. Response 조립과 {@code PaginationResponse} 매핑은 컨트롤러가 담당한다.
 * 댓글·답글도 계층 구조로 조립하지 않고 각각의 목록을 그대로 넘겨, 중첩 Response 조립을
 * {@code ReviewCommentListItemResponse.from(...)}이 담당한다.
 */
public interface ReviewQueryUseCase {

    PageResult<ReviewListItemResult> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    );

    ReviewManagementDetailResult getReview(Long id);

    List<ReviewCommentListItemResult> getComments(Long id);

    List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments);
}
