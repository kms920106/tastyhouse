package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewLikeSavePort {

    void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);

    ReviewLike save(ReviewLike reviewLike);
}
