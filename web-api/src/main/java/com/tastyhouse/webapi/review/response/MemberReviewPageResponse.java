package com.tastyhouse.webapi.review.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record MemberReviewPageResponse(
    List<MemberReviewListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static MemberReviewPageResponse from(PageResult<MemberReviewListItemResponse> pageResult) {
        return new MemberReviewPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
