package com.tastyhouse.webapi.search.response;

import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.external.file.FileService;

public record SearchReviewListItemResponse(
    Long id,
    String imageUrl
) {
    public static SearchReviewListItemResponse from(SearchReviewItemDto dto, FileService fileService) {
        return new SearchReviewListItemResponse(
            dto.id(),
            fileService.getUrlByPath(dto.imageFilePath())
        );
    }
}
