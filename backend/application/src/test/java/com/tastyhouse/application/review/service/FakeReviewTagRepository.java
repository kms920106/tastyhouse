package com.tastyhouse.application.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.store.ReviewTagRepository;

public class FakeReviewTagRepository implements ReviewTagRepository {
    @Override
    public void saveAll(List<ReviewTag> tags) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}
