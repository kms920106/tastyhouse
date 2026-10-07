package com.tastyhouse.application.review.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewCommentListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewCommentItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentListView;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyItemResult;

@Service
@Transactional(readOnly = true)
class ReviewCommentListQueryService implements ReviewCommentListQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;
    private final ReviewDetailReader reviewDetailReader;

    public ReviewCommentListQueryService(
        ReviewQueryPort reviewQueryPort,
        ReviewDetailReader reviewDetailReader
    ) {
        this.reviewQueryPort = reviewQueryPort;
        this.reviewDetailReader = reviewDetailReader;
    }

    @Override
    public ReviewCommentListView searchCommentsWithReplies(Long reviewId, Long viewerMemberId) {
        reviewDetailReader.requireVisibleReview(reviewId, viewerMemberId);

        List<ReviewCommentItemResult> comments = reviewQueryPort.findComments(ReviewId.of(reviewId).value());

        if (comments.isEmpty()) {
            return new ReviewCommentListView(List.of(), 0);
        }

        List<Long> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()).value())
            .toList();

        List<ReviewReplyItemResult> allReplies = reviewQueryPort.findVisibleReplies(commentIds);

        Map<Long, List<ReviewReplyItemResult>> repliesByCommentId = allReplies.stream()
            .collect(Collectors.groupingBy(ReviewReplyItemResult::commentId));

        List<ReviewCommentListView.CommentWithReplies> items = comments.stream()
            .map(comment -> new ReviewCommentListView.CommentWithReplies(
                comment,
                repliesByCommentId.getOrDefault(comment.id(), List.of())
            ))
            .toList();

        int totalCount = comments.size() + allReplies.size();
        return new ReviewCommentListView(items, totalCount);
    }
}
