package com.tastyhouse.webapplication.review.port.out;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewCommentItemResult;
import com.tastyhouse.application.review.port.out.ReviewReplyItemResult;

/**
 * 리뷰의 댓글·답글 목록 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. {@code totalCount}가 "댓글 수 + 답글 수"라는 <b>집계</b>라 목록 계약
 * 하나로는 표현되지 않는다 — 산술은 서비스에 남는다.
 *
 * <p>{@code comments}는 공용 계약을 그대로 나르되 답글을 함께 묶어야 해서
 * {@link CommentWithReplies}로 한 겹 감싼다 — 댓글은 숨김 포함, 답글은 숨김 제외라 두 조회의 결과를
 * 서비스가 {@code commentId}로 묶는다(기존 동작 유지).
 */
public record ReviewCommentListView(
    List<CommentWithReplies> comments,
    int totalCount
) {

    /**
     * 댓글 한 건과 그 답글 목록.
     *
     * <p>답글은 공용 계약 {@code ReviewReplyItemResult}를 그대로 나른다 — 표현에 필요한 필드가 이미
     * 전부 담겨 있어 다시 옮길 이유가 없다.
     */
    public record CommentWithReplies(
        ReviewCommentItemResult comment,
        List<ReviewReplyItemResult> replies
    ) {
    }
}
