package com.tastyhouse.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 태그 연결 write 포트의 인메모리 fake.
 */
public class FakeReviewTagRepository implements ReviewTagRepository {

    private final List<ReviewTag> reviewTags = new ArrayList<>();

    @Override
    public void saveAll(List<ReviewTag> tags) {
        this.reviewTags.addAll(tags);
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        reviewTags.removeIf(reviewTag -> reviewTag.getReviewId().equals(reviewId));
    }
}
