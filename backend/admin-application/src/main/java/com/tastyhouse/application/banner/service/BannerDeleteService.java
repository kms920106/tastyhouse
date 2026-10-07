package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.in.BannerDeleteCommand;
import com.tastyhouse.application.banner.port.in.BannerDeleteUseCase;
import com.tastyhouse.application.banner.port.out.write.BannerPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BannerDeleteService implements BannerDeleteUseCase {

    private final BannerPersistencePort bannerPersistencePort;

    public BannerDeleteService(BannerPersistencePort bannerPersistencePort) {
        this.bannerPersistencePort = bannerPersistencePort;
    }

    @Override
    public void deleteBanner(BannerDeleteCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.delete();
        bannerPersistencePort.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerPersistencePort.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BANNER_NOT_FOUND));
    }
}
