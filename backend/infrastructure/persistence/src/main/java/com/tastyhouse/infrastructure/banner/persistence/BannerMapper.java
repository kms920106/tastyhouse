package com.tastyhouse.infrastructure.banner.persistence;

import com.tastyhouse.application.banner.port.out.write.BannerState;

final class BannerMapper {
    private BannerMapper() {
    }

    static BannerState toState(BannerJpaEntity entity) {
        return new BannerState(
            entity.getId(),
            entity.getType(),
            entity.getTitle(),
            entity.getImageFileId(),
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

    static BannerJpaEntity toEntity(BannerState state) {
        return BannerJpaEntity.create(
            state.type(),
            state.title(),
            state.imageFileId(),
            state.linkUrl(),
            state.startDate(),
            state.endDate(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(BannerJpaEntity entity, BannerState state) {
        entity.applyChanges(
            state.type(),
            state.title(),
            state.imageFileId(),
            state.linkUrl(),
            state.startDate(),
            state.endDate(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }
}
