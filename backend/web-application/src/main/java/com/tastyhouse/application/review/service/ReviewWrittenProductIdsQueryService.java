package com.tastyhouse.application.review.service;

import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewWrittenProductIdsQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;

@Service
@Transactional(readOnly = true)
class ReviewWrittenProductIdsQueryService implements ReviewWrittenProductIdsQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;

    public ReviewWrittenProductIdsQueryService(ReviewQueryPort reviewQueryPort) {
        this.reviewQueryPort = reviewQueryPort;
    }

    @Override
    public Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds) {
        return reviewQueryPort.findReviewedProductIds(orderId, memberId, productIds);
    }
}
