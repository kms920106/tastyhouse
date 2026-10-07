package com.tastyhouse.application.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewReplyManagementListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;

@Service
@Transactional(readOnly = true)
class ReviewReplyManagementListQueryService implements ReviewReplyManagementListQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;

    public ReviewReplyManagementListQueryService(ReviewManagementQueryPort reviewManagementQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
    }

    @Override
    public List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments) {
        List<Long> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()).value())
            .toList();
        return reviewManagementQueryPort.findRepliesIncludingHidden(commentIds);
    }
}
