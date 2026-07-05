package com.tastyhouse.webapi.review.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record LatestReviewPageResponse(
    List<LatestReviewListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static LatestReviewPageResponse from(PageResult<LatestReviewListItemResponse> pageResult) {
        return new LatestReviewPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
