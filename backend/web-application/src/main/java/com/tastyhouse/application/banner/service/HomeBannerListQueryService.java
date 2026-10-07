package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.application.banner.port.in.HomeBannerListQueryUseCase;
import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.banner.port.out.BannerQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class HomeBannerListQueryService implements HomeBannerListQueryUseCase {

    private final BannerQueryPort bannerQueryPort;

    public HomeBannerListQueryService(BannerQueryPort bannerQueryPort) {
        this.bannerQueryPort = bannerQueryPort;
    }

    @Override
    public PageResult<BannerListItemResult> getHomeBanners(int page, int size) {
        return bannerQueryPort.findVisibleBannersByType(BannerType.HOME.name(), PageQuery.of(page, size));
    }
}
