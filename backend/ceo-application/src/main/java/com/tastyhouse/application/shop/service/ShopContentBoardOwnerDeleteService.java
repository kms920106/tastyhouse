package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
class ShopContentBoardOwnerDeleteService implements ShopContentBoardOwnerDeleteUseCase {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;
    private final ShopContentBoardOwnerReader shopContentBoardOwnerReader;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopContentBoardOwnerDeleteService(
        ShopContentBoardPersistencePort shopContentBoardPersistencePort,
        ShopContentBoardOwnerReader shopContentBoardOwnerReader,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
        this.shopContentBoardOwnerReader = shopContentBoardOwnerReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void deleteContentBoard(ShopContentBoardOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopContentBoard shopContentBoard = shopContentBoardOwnerReader.loadOwnedContentBoard(shopId, contentBoardId);
        String previousValue = describeContentBoard(shopContentBoard);

        shopContentBoardPersistencePort.deleteById(contentBoardId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            null
        );
    }

    private String describeContentBoard(ShopContentBoard shopContentBoard) {
        String label = shopContentBoard.getTopic().getDescription()
            + "/" + shopContentBoard.getContentType().getDescription();
        String body = shopContentBoard.getDescription();
        if (body == null || body.isBlank()) {
            body = shopContentBoard.getYoutubeUrl();
        }
        return body == null || body.isBlank() ? label : label + ": " + body;
    }
}
