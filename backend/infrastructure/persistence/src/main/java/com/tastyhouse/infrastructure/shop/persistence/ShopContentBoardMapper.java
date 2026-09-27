package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopContentBoardState;

final class ShopContentBoardMapper {
    private ShopContentBoardMapper() {
    }

    static ShopContentBoardState toState(ShopContentBoardJpaEntity entity) {
        return new ShopContentBoardState(
            entity.getId(),
            entity.getShopId(),
            entity.getContentType(),
            entity.getTopic(),
            entity.getImageFileId(),
            entity.getYoutubeUrl(),
            entity.getDescription(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopContentBoardJpaEntity toEntity(ShopContentBoardState state) {
        return ShopContentBoardJpaEntity.create(
            state.shopId(),
            state.contentType(),
            state.topic(),
            state.imageFileId(),
            state.youtubeUrl(),
            state.description(),
            state.hidden()
        );
    }

    static void applyChanges(ShopContentBoardJpaEntity entity, ShopContentBoardState state) {
        entity.applyChanges(
            state.topic(),
            state.imageFileId(),
            state.youtubeUrl(),
            state.description(),
            state.hidden()
        );
    }
}
