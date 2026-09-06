package com.tastyhouse.application.banner.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.banner.port.out.BannerDetailResult;
import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.banner.port.out.BannerManagementQueryPort;
import com.tastyhouse.application.banner.port.out.BannerSearchCondition;
import com.tastyhouse.application.banner.port.in.BannerManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class BannerManagementQueryService implements BannerManagementQueryUseCase {

    private final BannerManagementQueryPort bannerManagementQueryPort;

    public BannerManagementQueryService(BannerManagementQueryPort bannerManagementQueryPort) {
        this.bannerManagementQueryPort = bannerManagementQueryPort;
    }

    @Override
    public PageResult<BannerManagementListItemResult> getBanners(String type, String title, Boolean visible, int page, int size) {
        BannerType bannerType = type == null ? null : BannerType.from(type);
        BannerSearchCondition condition = BannerSearchCondition.of(bannerType, title, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return bannerManagementQueryPort.findAllBanners(condition, pageQuery);
    }

    @Override
    public BannerDetailResult getBanner(Long id) {
        return bannerManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
