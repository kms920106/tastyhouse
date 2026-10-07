package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.port.in.ShopContentBoardCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
class ShopContentBoardOwnerCreateService implements ShopContentBoardOwnerCreateUseCase {

    private static final long MAX_CONTENT_BOARD_COUNT = 4;

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopContentBoardOwnerCreateService(
        ShopContentBoardPersistencePort shopContentBoardPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String contentType = command.contentType();
        String topic = command.topic();
        String youtubeUrl = command.youtubeUrl();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        if (shopContentBoardPersistencePort.countByShopId(shopId) >= MAX_CONTENT_BOARD_COUNT) {
            throw new ApplicationException(CeoErrorCode.SHOP_CONTENT_BOARD_LIMIT_EXCEEDED);
        }

        ShopContentType type = ShopContentType.from(contentType);
        UploadedFileId imageFileId = uploadIfImage(type, file);

        ShopContentBoard shopContentBoard = ShopContentBoard.of(
            ShopId.of(shopId), type, ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        ShopContentBoard saved = shopContentBoardPersistencePort.save(shopContentBoard);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(ceoId),
            null,
            describeContentBoard(saved)
        );
        return saved.getId();
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

    private UploadedFileId uploadIfImage(ShopContentType contentType, MultipartFile file) {
        if (contentType == ShopContentType.VIDEO) {
            return null;
        }
        shopImageSpecValidator.validateContentImage(file, contentType == ShopContentType.GIF);
        return UploadedFileId.of(fileOwnerUploadUseCase.upload(file));
    }
}
