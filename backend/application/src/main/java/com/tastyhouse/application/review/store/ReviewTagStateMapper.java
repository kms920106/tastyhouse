package com.tastyhouse.application.review.store;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.application.review.port.out.write.ReviewTagState;

final class ReviewTagStateMapper {
    private ReviewTagStateMapper() {
    }

    static ReviewTag toDomain(ReviewTagState state) {
        return ReviewTag.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.tagId() == null ? null : TagId.of(state.tagId())
        );
    }

    static ReviewTagState toState(ReviewTag reviewTag) {
        return new ReviewTagState(
            reviewTag.getId(),
            reviewTag.getReviewId() == null ? null : reviewTag.getReviewId().value(),
            reviewTag.getTagId() == null ? null : reviewTag.getTagId().value()
        );
    }
}
