package com.tastyhouse.domain.review.domain.repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.model.ReviewLike;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

public interface ReviewLikeRepository {

    boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    ReviewLike save(ReviewLike reviewLike);
}
