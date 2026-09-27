package com.tastyhouse.application.review.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewLikeStatePort;

public class ReviewLikeStore implements ReviewLikeRepository {
    private final ReviewLikeStatePort reviewLikeStatePort;

    public ReviewLikeStore(ReviewLikeStatePort reviewLikeStatePort) {
        this.reviewLikeStatePort = reviewLikeStatePort;
    }

    @Override
    public boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        return reviewLikeStatePort.existsByReviewIdAndMemberId(reviewId.value(), memberId.value());
    }

    @Override
    public void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        reviewLikeStatePort.deleteByReviewIdAndMemberId(reviewId.value(), memberId.value());
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        return ReviewLikeStateMapper.toDomain(reviewLikeStatePort.save(ReviewLikeStateMapper.toState(reviewLike)));
    }
}
