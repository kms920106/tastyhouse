package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.model.ReviewTag;

public interface ReviewTagRepository {

    List<Long> findTagIdsByReviewId(Long reviewId);

    void saveAll(List<ReviewTag> tags);

    void deleteByReviewId(Long reviewId);
}
