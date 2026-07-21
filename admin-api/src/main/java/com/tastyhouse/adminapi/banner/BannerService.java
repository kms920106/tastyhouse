package com.tastyhouse.adminapi.banner;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.banner.application.BannerCommandService;
import com.tastyhouse.core.domain.banner.application.BannerQueryService;
import com.tastyhouse.core.domain.banner.application.dto.BannerSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerCreateCommand;
import com.tastyhouse.core.domain.banner.application.dto.command.BannerUpdateCommand;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerDetailResult;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerManagementListItemResult;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.response.BannerListItemResponse;
import com.tastyhouse.adminapi.file.response.FileResponse;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerCommandService bannerCommandService;
    private final BannerQueryService bannerQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public PaginationResponse<BannerListItemResponse> getBanners(String type, String title, Boolean visible, int page, int size) {
        BannerType bannerType = type == null ? null : BannerType.from(type);
        BannerSearchCondition condition = BannerSearchCondition.of(bannerType, title, visible);
        PageResult<BannerListItemResponse> pageResult = bannerQueryService.findAllBanners(condition, page, size)
            .map(this::toListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private BannerListItemResponse toListItemResponse(BannerManagementListItemResult dto) {
        return BannerListItemResponse.from(
            dto.id(),
            dto.type().name(),
            dto.title(),
            toFileResponse(dto),
            dto.linkUrl(),
            dto.startDate(),
            dto.endDate(),
            dto.sort(),
            dto.visible()
        );
    }

    private FileResponse toFileResponse(BannerManagementListItemResult dto) {
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
        BannerCreateCommand command = BannerCreateCommand.of(
            BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible
        );
        BannerId bannerId = bannerCommandService.createBanner(command);
        return bannerId.value();
    }

    public BannerDetailResponse getBanner(Long id) {
        BannerDetailResult bannerDetail = bannerQueryService.findDetailById(BannerId.of(id));
        return BannerDetailResponse.from(
            bannerDetail.bannerId().value(),
            bannerDetail.type().name(),
            bannerDetail.title(),
            toFileResponse(bannerDetail.imageFileId()),
            bannerDetail.linkUrl(),
            bannerDetail.startDate(),
            bannerDetail.endDate(),
            bannerDetail.sort(),
            bannerDetail.visible(),
            bannerDetail.createdAt(),
            bannerDetail.updatedAt()
        );
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
        BannerId bannerId = BannerId.of(id);
        BannerUpdateCommand command = BannerUpdateCommand.of(
            BannerType.from(type), title, imageFileId, linkUrl, startDate, endDate, sort, visible
        );
        bannerCommandService.updateBanner(bannerId, command);
    }

    public void deleteBanner(Long id) {
        BannerId bannerId = BannerId.of(id);
        bannerCommandService.deleteBanner(bannerId);
    }
}
