package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopContentBoardMapper {
    private ShopContentBoardMapper() {
    }

    static ShopContentBoard toDomain(ShopContentBoardJpaEntity entity) {
        return ShopContentBoard.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContentType(),
            entity.getTopic(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getYoutubeUrl(),
            entity.getDescription(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopContentBoardJpaEntity toEntity(ShopContentBoard domain) {
        return ShopContentBoardJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContentType(),
            domain.getTopic(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getYoutubeUrl(),
            domain.getDescription(),
            domain.isHidden()
        );
    }

    static void applyChanges(ShopContentBoardJpaEntity entity, ShopContentBoard domain) {
        entity.applyChanges(
            domain.getTopic(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getYoutubeUrl(),
            domain.getDescription(),
            domain.isHidden()
        );
    }
}
