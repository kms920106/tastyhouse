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
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopContentBoardUpdateCommand;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardPersistencePort;

@Service
@Transactional
class ShopContentBoardOwnerUpdateService implements ShopContentBoardOwnerUpdateUseCase {

    private final ShopContentBoardPersistencePort shopContentBoardPersistencePort;
    private final ShopContentBoardOwnerReader shopContentBoardOwnerReader;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopContentBoardOwnerUpdateService(
        ShopContentBoardPersistencePort shopContentBoardPersistencePort,
        ShopContentBoardOwnerReader shopContentBoardOwnerReader,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopContentBoardPersistencePort = shopContentBoardPersistencePort;
        this.shopContentBoardOwnerReader = shopContentBoardOwnerReader;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();
        String topic = command.topic();
        String youtubeUrl = command.youtubeUrl();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopContentBoard shopContentBoard = shopContentBoardOwnerReader.loadOwnedContentBoard(shopId, contentBoardId);

        String previousValue = describeContentBoard(shopContentBoard);

        UploadedFileId imageFileId = file != null && !file.isEmpty()
            ? uploadIfImage(shopContentBoard.getContentType(), file)
            : shopContentBoard.getImageFileId();

        shopContentBoard.update(ShopContentTopic.from(topic), imageFileId, youtubeUrl, description);
        shopContentBoardPersistencePort.save(shopContentBoard);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeContentBoard(shopContentBoard)
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

    private UploadedFileId uploadIfImage(ShopContentType contentType, MultipartFile file) {
        if (contentType == ShopContentType.VIDEO) {
            return null;
        }
        shopImageSpecValidator.validateContentImage(file, contentType == ShopContentType.GIF);
        return UploadedFileId.of(fileOwnerUploadUseCase.upload(file));
    }
}
