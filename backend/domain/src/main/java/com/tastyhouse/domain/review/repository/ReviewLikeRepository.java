package com.tastyhouse.domain.review.repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewLikeRepository {

    boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    ReviewLike save(ReviewLike reviewLike);
}
