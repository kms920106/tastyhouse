package com.tastyhouse.application.banner.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.banner.port.in.BannerCommandUseCase;
import com.tastyhouse.application.banner.port.in.BannerCreateCommand;
import com.tastyhouse.application.banner.port.in.BannerDeleteCommand;
import com.tastyhouse.application.banner.port.in.BannerUpdateCommand;
import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.banner.repository.BannerRepository;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class BannerCommandService implements BannerCommandUseCase {

    private final BannerRepository bannerRepository;

    public BannerCommandService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
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
        Banner saved = bannerRepository.save(banner);
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
        bannerRepository.save(banner);
    }

    @Override
    public void deleteBanner(BannerDeleteCommand command) {
        BannerId bannerId = BannerId.of(command.bannerId());
        Banner banner = findBannerOrThrow(bannerId);

        banner.delete();
        bannerRepository.save(banner);
    }

    private Banner findBannerOrThrow(BannerId bannerId) {
        return bannerRepository.findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BANNER_NOT_FOUND));
    }
}
