package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardSavePort;

@Service
@Transactional
class ShopContentBoardManagementDeleteService implements ShopContentBoardManagementDeleteUseCase {

    private final ShopContentBoardLoadPort shopContentBoardLoadPort;
    private final ShopContentBoardSavePort shopContentBoardSavePort;

    public ShopContentBoardManagementDeleteService(ShopContentBoardLoadPort shopContentBoardLoadPort, ShopContentBoardSavePort shopContentBoardSavePort) {
        this.shopContentBoardLoadPort = shopContentBoardLoadPort;
        this.shopContentBoardSavePort = shopContentBoardSavePort;
    }

    @Override
    public void deleteContentBoard(ShopContentBoardManagementDeleteCommand command) {
        Long contentBoardId = command.contentBoardId();
        verifyContentBoardExists(contentBoardId);
        shopContentBoardSavePort.deleteById(contentBoardId);
    }

    private void verifyContentBoardExists(Long contentBoardId) {
        shopContentBoardLoadPort.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}
