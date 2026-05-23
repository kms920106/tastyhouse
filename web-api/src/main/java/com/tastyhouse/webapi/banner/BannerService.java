package com.tastyhouse.webapi.banner;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import com.tastyhouse.core.service.BannerCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.banner.response.BannerListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCoreService bannerCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<BannerListItemResponse> findHomeBanners(int page, int size) {
        return PageResult.from(bannerCoreService.findHomeBanners(page, size)).map(this::toBannerListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<BannerListItemResponse> findSidebarBanners(int page, int size) {
        return PageResult.from(bannerCoreService.findSidebarBanners(page, size)).map(this::toBannerListItemResponse);
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
