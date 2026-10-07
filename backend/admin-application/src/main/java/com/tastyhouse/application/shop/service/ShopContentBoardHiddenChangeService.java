package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopContentBoardHiddenChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardHiddenChangeUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
class ShopContentBoardHiddenChangeService implements ShopContentBoardHiddenChangeUseCase {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;

    public ShopContentBoardHiddenChangeService(ShopContentBoardPersistencePort shopContentBoardPersistencePort) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
    }

    @Override
    public void changeHidden(ShopContentBoardHiddenChangeCommand command) {
        Long contentBoardId = command.contentBoardId();
        boolean hidden = command.hidden();
        ShopContentBoard shopContentBoard = loadContentBoard(contentBoardId);
        if (hidden) {
            shopContentBoard.hide();
        } else {
            shopContentBoard.unhide();
        }
        shopContentBoardPersistencePort.save(shopContentBoard);
    }

    private ShopContentBoard loadContentBoard(Long contentBoardId) {
        return shopContentBoardPersistencePort.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}
