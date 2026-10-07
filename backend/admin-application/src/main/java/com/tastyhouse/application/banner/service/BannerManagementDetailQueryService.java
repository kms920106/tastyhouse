package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.banner.port.in.BannerManagementDetailQueryUseCase;
import com.tastyhouse.application.banner.port.out.BannerDetailResult;
import com.tastyhouse.application.banner.port.out.BannerManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class BannerManagementDetailQueryService implements BannerManagementDetailQueryUseCase {

    private final BannerManagementQueryPort bannerManagementQueryPort;

    public BannerManagementDetailQueryService(BannerManagementQueryPort bannerManagementQueryPort) {
        this.bannerManagementQueryPort = bannerManagementQueryPort;
    }

    @Override
    public BannerDetailResult getBanner(Long id) {
        return bannerManagementQueryPort.findDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BANNER_NOT_FOUND));
    }
}
