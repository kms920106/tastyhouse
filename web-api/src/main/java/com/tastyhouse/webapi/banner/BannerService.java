package com.tastyhouse.webapi.banner;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import com.tastyhouse.core.service.BannerCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.banner.response.BannerListItem;
import com.tastyhouse.webapi.common.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCoreService bannerCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<BannerListItem> findHomeBanners(PageRequest pageRequest) {
        return bannerCoreService.findHomeBanners(pageRequest.page(), pageRequest.size()).map(this::toBannerListItem);
    }

    @Transactional(readOnly = true)
    public PageResult<BannerListItem> findSidebarBanners(PageRequest pageRequest) {
        return bannerCoreService.findSidebarBanners(pageRequest.page(), pageRequest.size()).map(this::toBannerListItem);
    }

    private BannerListItem toBannerListItem(BannerListItemDto dto) {
        return BannerListItem.from(dto.id(), dto.title(), fileService.getUrlByPath(dto.filePath()), dto.linkUrl());
    }
}
