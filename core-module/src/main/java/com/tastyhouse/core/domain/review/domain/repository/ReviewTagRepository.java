package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewTag;

import java.util.List;

public interface ReviewTagRepository {

    List<Long> findTagIdsByReviewId(Long reviewId);

    void saveAll(List<ReviewTag> tags);

    void deleteByReviewId(Long reviewId);
}
