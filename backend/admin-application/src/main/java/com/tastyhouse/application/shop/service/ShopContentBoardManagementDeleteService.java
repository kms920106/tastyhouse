package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
class ShopContentBoardManagementDeleteService implements ShopContentBoardManagementDeleteUseCase {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;

    public ShopContentBoardManagementDeleteService(ShopContentBoardPersistencePort shopContentBoardPersistencePort) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
    }

    @Override
    public void deleteContentBoard(ShopContentBoardManagementDeleteCommand command) {
        Long contentBoardId = command.contentBoardId();
        loadContentBoard(contentBoardId);
        shopContentBoardPersistencePort.deleteById(contentBoardId);
    }

    private ShopContentBoard loadContentBoard(Long contentBoardId) {
        return shopContentBoardPersistencePort.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}
