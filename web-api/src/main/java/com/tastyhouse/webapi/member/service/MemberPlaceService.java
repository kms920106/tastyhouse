package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MyBookmarkedPlaceListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final PlaceCoreService placeCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<MyBookmarkedPlaceListItemResponse> getMyBookmarkedPlaces(Long memberId, int page, int size) {
        return PageResult.from(placeCoreService.findMyBookmarkedPlaces(memberId, page, size))
            .map(dto -> MyBookmarkedPlaceListItemResponse.from(dto, fileService.getUrlByPath(dto.imageUrl())));
    }
}
