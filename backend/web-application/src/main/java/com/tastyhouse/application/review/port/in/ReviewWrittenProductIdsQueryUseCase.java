package com.tastyhouse.application.review.port.in;

import java.util.Collection;
import java.util.Set;

public interface ReviewWrittenProductIdsQueryUseCase {

    Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds);
}
