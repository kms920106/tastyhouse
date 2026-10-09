package com.tastyhouse.testsupport.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewImageSavePort;

public class FakeReviewImageSavePort implements ReviewImageSavePort {

    @Override
    public void saveAll(List<ReviewImage> images) {
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
    }
}
