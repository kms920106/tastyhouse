package com.tastyhouse.infrastructure.jpa.banner.persistence;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.file.vo.UploadedFileId;

final class BannerMapper {

    private BannerMapper() {
    }

    static Banner toDomain(BannerJpaEntity entity) {
        return Banner.reconstitute(
            entity.getId(),
            entity.getType() == null ? null : BannerType.valueOf(entity.getType()),
            entity.getTitle(),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getLinkUrl(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static BannerJpaEntity toEntity(Banner banner) {
        return BannerJpaEntity.create(
            banner.getType() == null ? null : banner.getType().name(),
            banner.getTitle(),
            banner.getImageFileId() == null ? null : banner.getImageFileId().value(),
            banner.getLinkUrl(),
            banner.getStartDate(),
            banner.getEndDate(),
            banner.getSort(),
            banner.isVisible(),
            banner.isDeleted()
        );
    }

    static void applyChanges(BannerJpaEntity entity, Banner banner) {
        entity.applyChanges(
            banner.getType() == null ? null : banner.getType().name(),
            banner.getTitle(),
            banner.getImageFileId() == null ? null : banner.getImageFileId().value(),
            banner.getLinkUrl(),
            banner.getStartDate(),
            banner.getEndDate(),
            banner.getSort(),
            banner.isVisible(),
            banner.isDeleted()
        );
    }
}
