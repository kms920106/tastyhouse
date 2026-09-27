package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardState;

final class ShopContentBoardStateMapper {
    private ShopContentBoardStateMapper() {
    }

    static ShopContentBoard toDomain(ShopContentBoardState state) {
        return ShopContentBoard.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.contentType() == null ? null : ShopContentType.valueOf(state.contentType()),
            state.topic() == null ? null : ShopContentTopic.valueOf(state.topic()),
            state.imageFileId() == null ? null : UploadedFileId.of(state.imageFileId()),
            state.youtubeUrl(),
            state.description(),
            state.hidden(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopContentBoardState toState(ShopContentBoard shopContentBoard) {
        return new ShopContentBoardState(
            shopContentBoard.getId(),
            shopContentBoard.getShopId() == null ? null : shopContentBoard.getShopId().value(),
            shopContentBoard.getContentType() == null ? null : shopContentBoard.getContentType().name(),
            shopContentBoard.getTopic() == null ? null : shopContentBoard.getTopic().name(),
            shopContentBoard.getImageFileId() == null ? null : shopContentBoard.getImageFileId().value(),
            shopContentBoard.getYoutubeUrl(),
            shopContentBoard.getDescription(),
            shopContentBoard.isHidden(),
            shopContentBoard.getCreatedAt(),
            shopContentBoard.getUpdatedAt()
        );
    }
}
