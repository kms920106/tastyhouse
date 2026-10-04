package com.tastyhouse.infrastructure.persistence.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopContentBoardMapper {

    private ShopContentBoardMapper() {
    }

    static ShopContentBoard toDomain(ShopContentBoardJpaEntity entity) {
        return ShopContentBoard.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getContentType() == null ? null : ShopContentType.valueOf(entity.getContentType()),
            entity.getTopic() == null ? null : ShopContentTopic.valueOf(entity.getTopic()),
            entity.getImageFileId() == null ? null : UploadedFileId.of(entity.getImageFileId()),
            entity.getYoutubeUrl(),
            entity.getDescription(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopContentBoardJpaEntity toEntity(ShopContentBoard shopContentBoard) {
        return ShopContentBoardJpaEntity.create(
            shopContentBoard.getShopId() == null ? null : shopContentBoard.getShopId().value(),
            shopContentBoard.getContentType() == null ? null : shopContentBoard.getContentType().name(),
            shopContentBoard.getTopic() == null ? null : shopContentBoard.getTopic().name(),
            shopContentBoard.getImageFileId() == null ? null : shopContentBoard.getImageFileId().value(),
            shopContentBoard.getYoutubeUrl(),
            shopContentBoard.getDescription(),
            shopContentBoard.isHidden()
        );
    }

    static void applyChanges(ShopContentBoardJpaEntity entity, ShopContentBoard shopContentBoard) {
        entity.applyChanges(
            shopContentBoard.getTopic() == null ? null : shopContentBoard.getTopic().name(),
            shopContentBoard.getImageFileId() == null ? null : shopContentBoard.getImageFileId().value(),
            shopContentBoard.getYoutubeUrl(),
            shopContentBoard.getDescription(),
            shopContentBoard.isHidden()
        );
    }
}
