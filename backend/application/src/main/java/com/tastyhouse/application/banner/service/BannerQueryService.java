package com.tastyhouse.application.banner.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.banner.port.out.BannerQueryPort;
import com.tastyhouse.application.banner.port.in.BannerQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class BannerQueryService implements BannerQueryUseCase {

    private final BannerQueryPort bannerQueryPort;

    public BannerQueryService(BannerQueryPort bannerQueryPort) {
        this.bannerQueryPort = bannerQueryPort;
    }

    @Override
    public PageResult<BannerListItemResult> getHomeBanners(int page, int size) {
        return getBannersByType(BannerType.HOME, page, size);
    }

    @Override
    public PageResult<BannerListItemResult> getSidebarBanners(int page, int size) {
        return getBannersByType(BannerType.SIDEBAR, page, size);
    }

    private PageResult<BannerListItemResult> getBannersByType(BannerType type, int page, int size) {
        return bannerQueryPort.findVisibleBannersByType(type, PageQuery.of(page, size));
    }
}
