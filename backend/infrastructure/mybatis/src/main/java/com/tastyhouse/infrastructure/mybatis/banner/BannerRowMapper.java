package com.tastyhouse.infrastructure.mybatis.banner;

import java.time.LocalDateTime;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;

final class BannerRowMapper {

    private BannerRowMapper() {
    }

    static Banner toDomain(BannerRow row) {
        return Banner.reconstitute(
            row.id(),
            row.type() == null ? null : BannerType.valueOf(row.type()),
            row.title(),
            row.imageFileId() == null ? null : UploadedFileId.of(row.imageFileId()),
            row.linkUrl(),
            row.startDate(),
            row.endDate(),
            row.sort(),
            row.visible(),
            row.deleted(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    static Banner toDomain(BannerWriteRow row) {
        return Banner.reconstitute(
            row.getId(),
            row.getType() == null ? null : BannerType.valueOf(row.getType()),
            row.getTitle(),
            row.getImageFileId() == null ? null : UploadedFileId.of(row.getImageFileId()),
            row.getLinkUrl(),
            row.getStartDate(),
            row.getEndDate(),
            row.getSort(),
            row.isVisible(),
            row.isDeleted(),
            row.getCreatedAt(),
            row.getUpdatedAt()
        );
    }

    static BannerWriteRow toWriteRow(Banner banner, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BannerWriteRow(
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
            createdAt,
            updatedAt
        );
    }
}
