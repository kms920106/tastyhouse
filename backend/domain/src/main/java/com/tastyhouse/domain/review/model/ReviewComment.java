package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;

public class ReviewComment {
    private final Long id;
    private final ReviewId reviewId;
    private final MemberId memberId;
    private final String content;
    private boolean hidden;
    private final LocalDateTime createdAt;

    private ReviewComment(
        Long id,
        ReviewId reviewId,
        MemberId memberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
        this.hidden = hidden;
        this.createdAt = createdAt;
    }

    public static ReviewComment of(ReviewId reviewId, MemberId memberId, String content) {
        return new ReviewComment(null, reviewId, memberId, content, false, null);
    }

    public static ReviewComment reconstitute(
        Long id,
        ReviewId reviewId,
        MemberId memberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ReviewComment(id, reviewId, memberId, content, hidden, createdAt);
    }

    public ReviewCommentId getReviewCommentId() {
        return ReviewCommentId.of(this.id);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
