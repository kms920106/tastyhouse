package com.tastyhouse.infrastructure.banner.persistence;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class BannerMapper {
    private BannerMapper() {
    }

    static Banner toDomain(BannerJpaEntity entity) {
        return Banner.reconstitute(
            entity.getId(),
            entity.getType(),
            entity.getTitle(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
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

    static BannerJpaEntity toEntity(Banner domain) {
        return BannerJpaEntity.create(
            domain.getType(),
            domain.getTitle(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getLinkUrl(),
            domain.getStartDate(),
            domain.getEndDate(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    static void applyChanges(BannerJpaEntity entity, Banner domain) {
        entity.applyChanges(
            domain.getType(),
            domain.getTitle(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getLinkUrl(),
            domain.getStartDate(),
            domain.getEndDate(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
