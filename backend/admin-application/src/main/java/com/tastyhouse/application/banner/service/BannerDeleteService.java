package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.in.BannerDeleteCommand;
import com.tastyhouse.application.banner.port.in.BannerDeleteUseCase;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BannerDeleteService implements BannerDeleteUseCase {

    private final BannerLoadPort bannerLoadPort;
    private final BannerSavePort bannerSavePort;

    public BannerDeleteService(BannerLoadPort bannerLoadPort, BannerSavePort bannerSavePort) {
        this.bannerLoadPort = bannerLoadPort;
        this.bannerSavePort = bannerSavePort;
    }

    @Override
    public void deleteBanner(BannerDeleteCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.delete();
        bannerSavePort.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerLoadPort.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BANNER_NOT_FOUND));
    }
}
