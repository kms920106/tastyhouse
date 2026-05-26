package com.tastyhouse.webapi.member.service;

import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.place.application.PlaceQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.PlaceBookmarkListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final PlaceQueryService placeQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponse<PlaceBookmarkListItemResponse> getMyBookmarkedPlaces(Long memberId, int page, int size) {
        return PageResponse.from(placeQueryService.findMyBookmarkedPlaces(memberId, page, size))
            .map(dto -> PlaceBookmarkListItemResponse.from(
                dto.placeId(),
                dto.bookmarkId(),
                dto.placeName(),
                dto.stationName(),
                dto.rating(),
                fileService.getUrlByPath(dto.imageUrl()),
                dto.bookmarked()
            ));
    }
}
