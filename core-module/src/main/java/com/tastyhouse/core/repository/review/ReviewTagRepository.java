package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewTag;

import java.util.List;

public interface ReviewTagRepository {

    List<Long> findTagIdsByReviewId(Long reviewId);

    void saveAll(List<ReviewTag> tags);

    void deleteByReviewId(Long reviewId);
}
