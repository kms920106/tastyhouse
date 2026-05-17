package com.tastyhouse.webapi.search.response;

import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.external.file.FileService;

public record SearchReviewListItem(
    Long reviewId,
    String imageUrl,
    Long placeId
) {
    public static SearchReviewListItem from(SearchReviewItemDto dto, FileService fileService) {
        return new SearchReviewListItem(
            dto.reviewId(),
            fileService.getUrlByPath(dto.imageFilePath()),
            dto.placeId()
        );
    }
}
