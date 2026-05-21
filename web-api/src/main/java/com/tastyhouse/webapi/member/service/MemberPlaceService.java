package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.PlaceBookmarkListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final PlaceCoreService placeCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<PlaceBookmarkListItemResponse> getMyBookmarkedPlaces(Long memberId, int page, int size) {
        return PageResult.from(placeCoreService.findMyBookmarkedPlaces(memberId, page, size))
            .map(dto -> PlaceBookmarkListItemResponse.from(
                dto.placeId(),
                dto.bookmarkId(),
                dto.placeName(),
                dto.stationName(),
                dto.rating(),
                fileService.getUrlByPath(dto.imageUrl()),
                dto.isBookmarked()
            ));
    }
}
