package com.tastyhouse.application.review.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.application.review.port.in.ShopReviewBlindReasonListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewBlindReasonView;

@Service
@Transactional(readOnly = true)
class ShopReviewBlindReasonListQueryService implements ShopReviewBlindReasonListQueryUseCase {

    @Override
    public List<ReviewBlindReasonView> getBlindReasons() {
        return Arrays.stream(ReviewBlindReason.values())
            .map(reason -> new ReviewBlindReasonView(reason.name(), reason.getDescription()))
            .toList();
    }
}
