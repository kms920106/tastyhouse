package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewLatestListQueryUseCase {

    PageResult<LatestReviewListItemResult> searchLatestReviewList(int page, int size, String type, Long memberId);
}
