package com.tastyhouse.adminapi.banner;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.response.BannerListItemResponse;
import com.tastyhouse.adminapi.banner.response.BannerPageResponse;
import com.tastyhouse.core.domain.banner.application.BannerCommandService;
import com.tastyhouse.core.domain.banner.application.BannerQueryService;
import com.tastyhouse.core.domain.banner.application.dto.BannerDetailDto;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerCreateCommand;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerUpdateCommand;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCommandService bannerCommandService;
    private final BannerQueryService bannerQueryService;

    public BannerPageResponse getBanners(BannerType type, int page, int size) {
        PageResult<BannerListItemResponse> pageResult = bannerQueryService.findAllForAdmin(type, page, size)
            .map(BannerListItemResponse::from);
        return BannerPageResponse.from(pageResult);
    }

    public Long createBanner(
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        BannerId bannerId = bannerCommandService.createBanner(
            BannerCreateCommand.of(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible)
        );
        return bannerId.value();
    }

    public BannerDetailResponse getBanner(Long id) {
        BannerDetailDto bannerDetail = bannerQueryService.findDetailById(BannerId.of(id));
        return BannerDetailResponse.from(bannerDetail);
    }

    public void updateBanner(
        Long id,
        BannerType type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        bannerCommandService.updateBanner(
            BannerId.of(id),
            BannerUpdateCommand.of(type, title, imageFileId, linkUrl, startDate, endDate, sort, visible)
        );
    }

    public void deleteBanner(Long id) {
        bannerCommandService.deleteBanner(BannerId.of(id));
    }
}
