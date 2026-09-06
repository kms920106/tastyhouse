package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardHiddenChangeCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class ShopContentBoardManagementCommandService implements ShopContentBoardManagementCommandUseCase {

    private final ShopContentBoardRepository shopContentBoardRepository;

    public ShopContentBoardManagementCommandService(ShopContentBoardRepository shopContentBoardRepository) {
        this.shopContentBoardRepository = shopContentBoardRepository;
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
        shopContentBoardRepository.save(shopContentBoard);
    }

    @Override
    public void deleteContentBoard(ShopContentBoardManagementDeleteCommand command) {
        Long contentBoardId = command.contentBoardId();
        loadContentBoard(contentBoardId);
        shopContentBoardRepository.deleteById(contentBoardId);
    }

    private ShopContentBoard loadContentBoard(Long contentBoardId) {
        return shopContentBoardRepository.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
    }
}
