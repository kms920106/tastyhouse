package com.tastyhouse.testsupport.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewTagSavePort;

public class FakeReviewTagSavePort implements ReviewTagSavePort {

    @Override
    public void saveAll(List<ReviewTag> tags) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}
