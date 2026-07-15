package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.ShopBookmarkListItemResponse;

@Service
@RequiredArgsConstructor
public class MemberShopService {

    private final ShopQueryService shopQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<ShopBookmarkListItemResponse> getMyBookmarkedShops(Long memberId, int page, int size) {
        return shopQueryService.findMyBookmarkedShops(MemberId.of(memberId), page, size)
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
