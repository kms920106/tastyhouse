package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewMemberListQueryUseCase {

    PageResult<MyReviewListItemResult> findMemberReviews(Long memberId, int page, int size);
}
