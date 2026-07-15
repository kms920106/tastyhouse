package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.model.ReviewLike;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

public interface ReviewLikeRepository {

    boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    ReviewLike save(ReviewLike reviewLike);
}
