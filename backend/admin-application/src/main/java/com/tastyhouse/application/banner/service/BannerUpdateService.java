package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.banner.port.in.BannerUpdateCommand;
import com.tastyhouse.application.banner.port.in.BannerUpdateUseCase;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class BannerUpdateService implements BannerUpdateUseCase {

    private final BannerLoadPort bannerLoadPort;
    private final BannerSavePort bannerSavePort;

    public BannerUpdateService(BannerLoadPort bannerLoadPort, BannerSavePort bannerSavePort) {
        this.bannerLoadPort = bannerLoadPort;
        this.bannerSavePort = bannerSavePort;
    }

    @Override
    public void updateBanner(BannerUpdateCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.update(
            BannerType.from(command.type()),
            command.title(),
            UploadedFileId.of(command.imageFileId()),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        bannerSavePort.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerLoadPort.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.BANNER_NOT_FOUND));
    }
}
