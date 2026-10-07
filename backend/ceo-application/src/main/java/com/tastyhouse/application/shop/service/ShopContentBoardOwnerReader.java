package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Component
class ShopContentBoardOwnerReader {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;

    public ShopContentBoardOwnerReader(ShopContentBoardPersistencePort shopContentBoardPersistencePort) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
    }

    public ShopContentBoard loadOwnedContentBoard(Long shopId, Long contentBoardId) {
        ShopContentBoard shopContentBoard = shopContentBoardPersistencePort.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
        if (!shopContentBoard.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND);
        }
        return shopContentBoard;
    }
}
