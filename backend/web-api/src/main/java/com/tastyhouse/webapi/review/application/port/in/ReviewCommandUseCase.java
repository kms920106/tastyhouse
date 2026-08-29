package com.tastyhouse.webapi.review.application.port.in;

/**
 * 리뷰 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewCommandService})을 알지 않는다.
 *
 * <p>{@code findReviewIdOfComment}는 조회처럼 보이지만 <b>답글 등록 경로의 가시성 가드 입력</b>이다 —
 * 답글 경로는 {@code commentId}만 받아 상위 리뷰를 알 수 없으므로, 이것 없이는 보이지 않는 리뷰의
 * 댓글 스레드에 답글을 붙일 수 있다. 쓰기 유스케이스의 일부라 이 포트에 둔다.
 */
public interface ReviewCommandUseCase {

    Long createReview(ReviewCreateCommand command);

    Long updateReview(ReviewUpdateCommand command);

    void deleteReview(ReviewDeleteCommand command);

    boolean toggleReviewLike(ReviewLikeToggleCommand command);

    Long createComment(ReviewCommentCreateCommand command);

    Long findReviewIdOfComment(Long commentId);

    Long createReply(ReviewReplyCreateCommand command);
}
