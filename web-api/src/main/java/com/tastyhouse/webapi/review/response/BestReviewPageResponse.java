package com.tastyhouse.webapi.review.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record BestReviewPageResponse(
    List<BestReviewListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static BestReviewPageResponse from(PageResult<BestReviewListItemResponse> pageResult) {
        return new BestReviewPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
