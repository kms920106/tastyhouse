package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.application.shop.port.in.ShopContentBoardHiddenChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteCommand;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
public class ShopContentBoardManagementCommandService implements ShopContentBoardManagementCommandUseCase {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;

    public ShopContentBoardManagementCommandService(ShopContentBoardPersistencePort shopContentBoardPersistencePort) {
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

    @Override
    public void deleteContentBoard(ShopContentBoardManagementDeleteCommand command) {
        Long contentBoardId = command.contentBoardId();
        loadContentBoard(contentBoardId);
        shopContentBoardPersistencePort.deleteById(contentBoardId);
    }

    private ShopContentBoard loadContentBoard(Long contentBoardId) {
        return shopContentBoardPersistencePort.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}
