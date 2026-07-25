package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;
import com.tastyhouse.core.domain.shop.domain.repository.ShopContentBoardRepository;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopContentBoardCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopContentBoardUpdateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopContentBoardCommandService {

    private static final long MAX_CONTENT_BOARD_COUNT = 4;

    private final ShopContentBoardRepository shopContentBoardRepository;

    public Long createContentBoard(ShopContentBoardCreateCommand command) {
        long count = shopContentBoardRepository.countByShopId(command.shopId());
        if (count >= MAX_CONTENT_BOARD_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_BOARD_LIMIT_EXCEEDED);
        }

        ShopContentBoard shopContentBoard = ShopContentBoard.of(
            command.shopId(),
            command.contentType(),
            command.topic(),
            command.imageFileId(),
            command.youtubeUrl(),
            command.description()
        );

        ShopContentBoard saved = shopContentBoardRepository.save(shopContentBoard);
        return saved.getId();
    }

    public void updateContentBoard(Long id, ShopContentBoardUpdateCommand command) {
        ShopContentBoard shopContentBoard = shopContentBoardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));

        shopContentBoard.update(command.topic(), command.imageFileId(), command.youtubeUrl(), command.description());
        shopContentBoardRepository.save(shopContentBoard);
    }

    public void deleteContentBoard(Long id) {
        shopContentBoardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));

        shopContentBoardRepository.deleteById(id);
    }

    public void changeHidden(Long id, boolean hidden) {
        ShopContentBoard shopContentBoard = shopContentBoardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));

        if (hidden) {
            shopContentBoard.hide();
        } else {
            shopContentBoard.unhide();
        }
        shopContentBoardRepository.save(shopContentBoard);
    }
}
