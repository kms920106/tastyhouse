package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewManagementListQueryUseCase {

    PageResult<ReviewListItemResult> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    );
}
