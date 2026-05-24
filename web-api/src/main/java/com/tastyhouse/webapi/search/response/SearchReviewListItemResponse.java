package com.tastyhouse.webapi.search.response;

import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.external.file.FileService;

public record SearchReviewListItemResponse(
    Long id,
    String imageUrl
) {
    public static SearchReviewListItemResponse from(SearchReviewItemResult dto, FileService fileService) {
        return new SearchReviewListItemResponse(
            dto.id(),
            fileService.getUrlByPath(dto.imageFilePath())
        );
    }
}
