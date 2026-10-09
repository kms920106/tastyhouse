package com.tastyhouse.application.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.banner.port.in.BannerCreateCommand;
import com.tastyhouse.application.banner.port.in.BannerCreateUseCase;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;

@Service
@Transactional
class BannerCreateService implements BannerCreateUseCase {

    private final BannerSavePort bannerSavePort;

    public BannerCreateService(BannerSavePort bannerSavePort) {
        this.bannerSavePort = bannerSavePort;
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
        Banner saved = bannerSavePort.save(banner);
        return saved.getBannerId().value();
    }
}
