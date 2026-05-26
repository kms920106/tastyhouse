package com.tastyhouse.webapi.banner;

import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.banner.application.BannerQueryService;
import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.banner.response.BannerListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerQueryService bannerQueryService;
    private final FileService fileService;

    public PageResponse<BannerListItemResponse> findHomeBanners(int page, int size) {
        return PageResponse.from(bannerQueryService.findHomeBanners(page, size)).map(this::toBannerListItemResponse);
    }

    public PageResponse<BannerListItemResponse> findSidebarBanners(int page, int size) {
        return PageResponse.from(bannerQueryService.findSidebarBanners(page, size)).map(this::toBannerListItemResponse);
    }

    private BannerListItemResponse toBannerListItemResponse(BannerListItemDto dto) {
        return BannerListItemResponse.from(
            dto.id(),
            dto.title(),
            fileService.getUrlByPath(dto.filePath()),
            dto.linkUrl()
        );
    }
}
