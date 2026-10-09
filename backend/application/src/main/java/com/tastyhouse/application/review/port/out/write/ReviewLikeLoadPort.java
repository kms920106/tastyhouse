package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewLikeLoadPort {

    boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId);
}
