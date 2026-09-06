package com.tastyhouse.domain.review.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.review.vo.ReviewId;

public class ReviewImage {
    private final Long id;
    private final ReviewId reviewId;
    private final UploadedFileId imageFileId;
    private final Integer sort;

    private ReviewImage(Long id, ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        this.id = id;
        this.reviewId = reviewId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static ReviewImage of(ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        return new ReviewImage(null, reviewId, imageFileId, sort);
    }

    public static ReviewImage reconstitute(Long id, ReviewId reviewId, UploadedFileId imageFileId, Integer sort) {
        return new ReviewImage(id, reviewId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
