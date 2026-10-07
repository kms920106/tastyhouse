package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewMyListQueryUseCase {

    PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size);
}
