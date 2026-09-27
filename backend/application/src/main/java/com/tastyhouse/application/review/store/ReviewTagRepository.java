package com.tastyhouse.application.review.store;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewTagRepository {
    void saveAll(List<ReviewTag> tags);

    void deleteByReviewId(ReviewId reviewId);
}
