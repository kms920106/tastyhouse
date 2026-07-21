package com.tastyhouse.webapi.banner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.banner.application.BannerQueryService;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.banner.response.BannerListItemResponse;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerQueryService bannerQueryService;
    private final FileService fileService;

    public PageResult<BannerListItemResponse> findHomeBanners(int page, int size) {
        return bannerQueryService.findHomeBanners(page, size).map(this::toBannerListItemResponse);
    }

    public PageResult<BannerListItemResponse> findSidebarBanners(int page, int size) {
        return bannerQueryService.findSidebarBanners(page, size).map(this::toBannerListItemResponse);
    }

    private BannerListItemResponse toBannerListItemResponse(BannerListItemResult dto) {
        return BannerListItemResponse.from(
            dto.id(),
            dto.title(),
            fileService.getUrlByPath(dto.filePath()),
            dto.linkUrl()
        );
    }
}
