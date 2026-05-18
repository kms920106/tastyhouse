package com.tastyhouse.webapi.search.response;

import com.tastyhouse.core.entity.review.dto.SearchReviewItemDto;
import com.tastyhouse.external.file.FileService;

public record SearchReviewListItem(
    Long id,
    String imageUrl
) {
    public static SearchReviewListItem from(SearchReviewItemDto dto, FileService fileService) {
        return new SearchReviewListItem(
            dto.id(),
            fileService.getUrlByPath(dto.imageFilePath())
        );
    }
}
