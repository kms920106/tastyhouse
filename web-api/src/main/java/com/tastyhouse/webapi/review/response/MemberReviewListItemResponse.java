package com.tastyhouse.webapi.review.response;

import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;

public record MemberReviewListItemResponse(
        Long id,
        String imageUrl
) {
    public static MemberReviewListItemResponse from(MyReviewListItemDto dto, String imageUrl) {
        return new MemberReviewListItemResponse(dto.id(), imageUrl);
    }
}
