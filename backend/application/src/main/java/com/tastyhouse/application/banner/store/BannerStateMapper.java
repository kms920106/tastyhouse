package com.tastyhouse.application.banner.store;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.application.banner.port.out.write.BannerState;

final class BannerStateMapper {
    private BannerStateMapper() {
    }

    static Banner toDomain(BannerState state) {
        return Banner.reconstitute(
            state.id(),
            state.type() == null ? null : BannerType.valueOf(state.type()),
            state.title(),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.linkUrl(),
            state.startDate(),
            state.endDate(),
            state.sort(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static BannerState toState(Banner banner) {
        return new BannerState(
            banner.getId(),
            banner.getType() == null ? null : banner.getType().name(),
            banner.getTitle(),
            banner.getImageFileId() == null ? null : banner.getImageFileId().value(),
            banner.getLinkUrl(),
            banner.getStartDate(),
            banner.getEndDate(),
            banner.getSort(),
            banner.isVisible(),
            banner.isDeleted(),
            banner.getCreatedAt(),
            banner.getUpdatedAt()
        );
    }
}
