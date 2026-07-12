package com.tastyhouse.adminapi.banner;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.banner.application.BannerCommandService;
import com.tastyhouse.core.domain.banner.application.BannerQueryService;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.BannerDetailDto;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerCreateCommand;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerUpdateCommand;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.response.BannerListItemResponse;
import com.tastyhouse.adminapi.banner.response.BannerPageResponse;
import com.tastyhouse.adminapi.common.FileResponse;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCommandService bannerCommandService;
    private final BannerQueryService bannerQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public BannerPageResponse getBanners(String type, String title, Boolean visible, int page, int size) {
        BannerType bannerType = type == null ? null : BannerType.from(type);
        BannerAdminSearchCondition condition = BannerAdminSearchCondition.of(bannerType, title, visible);
        PageResult<BannerListItemResponse> pageResult = bannerQueryService.findAllForAdmin(condition, page, size)
            .map(this::toListItemResponse);
        return BannerPageResponse.from(pageResult);
    }

    private BannerListItemResponse toListItemResponse(BannerAdminListItemDto dto) {
        return BannerListItemResponse.from(dto, toFileResponse(dto));
    }

    private FileResponse toFileResponse(BannerAdminListItemDto dto) {
        if (dto.imageFileId() == null) {
            return null;
        }
        return FileResponse.of(dto.imageFileId(), dto.imageFileName(), fileService.getUrlByPath(dto.imageFilePath()));
    }

    public Long createBanner(
        String type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        BannerId bannerId = bannerCommandService.createBanner(
            BannerCreateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible)
        );
        return bannerId.value();
    }

    public BannerDetailResponse getBanner(Long id) {
        BannerDetailDto bannerDetail = bannerQueryService.findDetailById(BannerId.of(id));
        return BannerDetailResponse.from(bannerDetail, toFileResponse(bannerDetail.imageFileId()));
    }

    private FileResponse toFileResponse(Long imageFileId) {
        if (imageFileId == null) {
            return null;
        }
        return fileQueryService.findById(UploadedFileId.of(imageFileId))
            .map(file -> FileResponse.of(file.getId(), file.getOriginalFilename(), fileService.getUrlByPath(file.getFilePath())))
            .orElse(null);
    }

    public void updateBanner(
        Long id,
        String type,
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
            BannerUpdateCommand.of(BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible)
        );
    }

    public void deleteBanner(Long id) {
        bannerCommandService.deleteBanner(BannerId.of(id));
    }
}
