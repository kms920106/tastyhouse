package com.tastyhouse.core.domain.banner.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerCreateCommand;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerUpdateCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class BannerCommandService {

    private final BannerRepository bannerRepository;

    public BannerId createBanner(BannerCreateCommand command) {
        Banner banner = Banner.of(
            command.type(),
            command.title(),
            command.imageFileId(),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        Banner saved = bannerRepository.save(banner);
        return saved.getBannerId();
    }

    public void updateBanner(BannerId bannerId, BannerUpdateCommand command) {
        Banner banner = bannerRepository.findById(bannerId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BANNER_NOT_FOUND));

        banner.update(
            command.type(),
            command.title(),
            command.imageFileId(),
            command.linkUrl(),
            command.startDate(),
            command.endDate(),
            command.sort(),
            command.visible()
        );
        bannerRepository.save(banner);
    }

    public void deleteBanner(BannerId bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BANNER_NOT_FOUND));

        banner.delete();
        bannerRepository.save(banner);
    }
}
