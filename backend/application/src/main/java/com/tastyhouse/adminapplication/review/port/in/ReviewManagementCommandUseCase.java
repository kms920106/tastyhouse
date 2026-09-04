package com.tastyhouse.adminapplication.review.port.in;

/**
 * 리뷰 관리 쓰기 인바운드 포트(admin).
 *
 * <p>리뷰·댓글·답글 3개 하위 자원의 숨김 전환과 삭제로 메서드 6개 — 분해 기준(7개 초과) 미만이고
 * 컨트롤러 하나만 공유하므로 단일 인터페이스로 둔다.
 */
public interface ReviewManagementCommandUseCase {

    void changeReviewHidden(ReviewHiddenChangeCommand command);

    void deleteReview(ReviewManagementDeleteCommand command);

    void changeCommentHidden(ReviewCommentHiddenChangeCommand command);

    void deleteComment(ReviewCommentDeleteCommand command);

    void changeReplyHidden(ReviewReplyHiddenChangeCommand command);

    void deleteReply(ReviewReplyDeleteCommand command);
}
