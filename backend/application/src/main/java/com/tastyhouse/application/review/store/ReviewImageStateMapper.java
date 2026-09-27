package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewImageState;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;

final class ReviewImageStateMapper {
    private ReviewImageStateMapper() {
    }

    static ReviewImage toDomain(ReviewImageState state) {
        return ReviewImage.reconstitute(
            state.id(),
            state.reviewId() == null ? null : ReviewId.of(state.reviewId()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.sort()
        );
    }

    static ReviewImageState toState(ReviewImage reviewImage) {
        return new ReviewImageState(
            reviewImage.getId(),
            reviewImage.getReviewId() == null ? null : reviewImage.getReviewId().value(),
            reviewImage.getImageFileId() == null ? null : reviewImage.getImageFileId().value(),
            reviewImage.getSort()
        );
    }
}
