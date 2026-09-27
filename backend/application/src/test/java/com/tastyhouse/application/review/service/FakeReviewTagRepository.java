package com.tastyhouse.application.review.service;

import java.util.List;

import com.tastyhouse.application.review.store.ReviewTagRepository;
import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;

public class FakeReviewTagRepository implements ReviewTagRepository {
    @Override
    public void saveAll(List<ReviewTag> tags) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}
