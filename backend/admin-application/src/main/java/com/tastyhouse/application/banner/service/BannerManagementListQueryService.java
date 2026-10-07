package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.application.banner.port.in.BannerManagementListQueryUseCase;
import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.banner.port.out.BannerManagementQueryPort;
import com.tastyhouse.application.banner.port.out.BannerSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class BannerManagementListQueryService implements BannerManagementListQueryUseCase {

    private final BannerManagementQueryPort bannerManagementQueryPort;

    public BannerManagementListQueryService(BannerManagementQueryPort bannerManagementQueryPort) {
        this.bannerManagementQueryPort = bannerManagementQueryPort;
    }

    @Override
    public PageResult<BannerManagementListItemResult> getBanners(String type, String title, Boolean visible, int page, int size) {
        String bannerType = type == null ? null : BannerType.from(type).name();
        BannerSearchCondition condition = BannerSearchCondition.of(bannerType, title, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerManagementQueryPort.findAllBanners(condition, pageQuery);
    }
}
