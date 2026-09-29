package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.banner.port.in.BannerCommandUseCase;
import com.tastyhouse.application.banner.port.in.BannerCreateCommand;
import com.tastyhouse.application.banner.port.in.BannerDeleteCommand;
import com.tastyhouse.application.banner.port.in.BannerUpdateCommand;
import com.tastyhouse.application.banner.port.out.write.BannerPersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;

@Service
@AdminApp
@Transactional
public class BannerCommandService implements BannerCommandUseCase {

    private final BannerPersistencePort bannerPersistencePort;

    public BannerCommandService(BannerPersistencePort bannerPersistencePort) {
        this.bannerPersistencePort = bannerPersistencePort;
    }

    @Override
    public Long createBanner(BannerCreateCommand command) {
        Banner banner = Banner.of(
            BannerType.from(command.type()),
            command.title(),
            UploadedFileId.of(command.imageFileId()),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        Banner saved = bannerPersistencePort.save(banner);
        return saved.getBannerId().value();
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
        bannerPersistencePort.save(banner);
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
