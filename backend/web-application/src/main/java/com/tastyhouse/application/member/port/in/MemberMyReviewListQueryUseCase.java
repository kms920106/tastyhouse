package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MemberMyReviewListQueryUseCase {

    PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size);
}
