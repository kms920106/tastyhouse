package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.webapi.member.response.MyBookmarkedPlaceListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberPlaceService {

    private final PlaceCoreService placeCoreService;

    // 내가 북마크한 장소 목록을 페이지네이션하여 조회
    @Transactional(readOnly = true)
    public PageResult<MyBookmarkedPlaceListItemResponse> getMyBookmarkedPlaces(Long memberId, int page, int size) {
        Page<MyBookmarkedPlaceItemDto> pageData = placeCoreService.findMyBookmarkedPlaces(memberId, page, size);

        List<MyBookmarkedPlaceListItemResponse> content = pageData.getContent().stream()
            .map(MyBookmarkedPlaceListItemResponse::from)
            .collect(Collectors.toList());

        return new PageResult<>(
            content,
            pageData.getTotalElements(),
            pageData.getTotalPages(),
            pageData.getNumber(),
            pageData.getSize()
        );
    }
}
