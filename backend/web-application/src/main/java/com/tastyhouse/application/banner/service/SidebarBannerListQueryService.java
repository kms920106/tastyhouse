package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.application.banner.port.in.SidebarBannerListQueryUseCase;
import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.banner.port.out.BannerQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class SidebarBannerListQueryService implements SidebarBannerListQueryUseCase {

    private final BannerQueryPort bannerQueryPort;

    public SidebarBannerListQueryService(BannerQueryPort bannerQueryPort) {
        this.bannerQueryPort = bannerQueryPort;
    }

    @Override
    public PageResult<BannerListItemResult> getSidebarBanners(int page, int size) {
        return getBannersByType(BannerType.SIDEBAR, page, size);
    }

    private PageResult<BannerListItemResult> getBannersByType(BannerType type, int page, int size) {
        return bannerQueryPort.findVisibleBannersByType(type.name(), PageQuery.of(page, size));
    }
}
