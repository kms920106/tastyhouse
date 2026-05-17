package com.tastyhouse.webapi.search.response;

import com.tastyhouse.core.entity.place.FoodType;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.SearchPlaceItemDto;
import com.tastyhouse.external.file.FileService;

import java.util.List;

public record SearchPlaceListItem(
    Long id,
    String name,
    String stationName,
    Double rating,
    String imageUrl,
    List<FoodType> foodTypes,
    Boolean isBookmarked
) {
    public static SearchPlaceListItem from(BestPlaceItemDto dto, FileService fileService) {
        return new SearchPlaceListItem(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
            dto.foodTypes(),
            false
        );
    }

    public static SearchPlaceListItem from(SearchPlaceItemDto dto, FileService fileService) {
        return new SearchPlaceListItem(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl() != null ? fileService.getUrlByPath(dto.imageUrl()) : null,
            dto.foodTypes(),
            dto.isBookmarked()
        );
    }
}
