package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewBestListQueryUseCase {

    PageResult<BestReviewListItemResult> searchBestReviewList(int page, int size);
}
