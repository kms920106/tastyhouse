package com.tastyhouse.webapi.member.service;

import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberShopService {

    private final ShopQueryService shopQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponse<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
        return PageResponse.from(shopQueryService.findMyBookmarkedShops(memberId, page, size))
            .map(dto -> ShopBookmarkListItemResponse.from(
                dto.shopId(),
                dto.bookmarkId(),
                dto.shopName(),
                dto.stationName(),
                dto.rating(),
                fileService.getUrlByPath(dto.imageUrl()),
                dto.bookmarked()
            ));
    }
}
