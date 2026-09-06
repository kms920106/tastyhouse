package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
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
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopContentBoardCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopContentBoardUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopContentBoardOwnerCommandService implements ShopContentBoardOwnerCommandUseCase {

    private static final long MAX_CONTENT_BOARD_COUNT = 4;

    private final ShopContentBoardRepository shopContentBoardRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopContentBoardOwnerCommandService(
        ShopContentBoardRepository shopContentBoardRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopContentBoardRepository = shopContentBoardRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
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

        if (shopContentBoardRepository.countByShopId(shopId) >= MAX_CONTENT_BOARD_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_BOARD_LIMIT_EXCEEDED);
        }

        ShopContentType type = ShopContentType.from(contentType);
        UploadedFileId imageFileId = uploadIfImage(type, file);

        ShopContentBoard shopContentBoard = ShopContentBoard.of(
            ShopId.of(shopId), type, ShopContentTopic.from(topic), imageFileId, youtubeUrl, description
        );
        ShopContentBoard saved = shopContentBoardRepository.save(shopContentBoard);

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

    @Override
    public void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();
        String topic = command.topic();
        String youtubeUrl = command.youtubeUrl();
        String description = command.description();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopContentBoard shopContentBoard = loadOwnedContentBoard(shopId, contentBoardId);

        String previousValue = describeContentBoard(shopContentBoard);

        UploadedFileId imageFileId = file != null && !file.isEmpty()
            ? uploadIfImage(shopContentBoard.getContentType(), file)
            : shopContentBoard.getImageFileId();

        shopContentBoard.update(ShopContentTopic.from(topic), imageFileId, youtubeUrl, description);
        shopContentBoardRepository.save(shopContentBoard);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONTENT_BOARD,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeContentBoard(shopContentBoard)
        );
    }

    @Override
    public void deleteContentBoard(ShopContentBoardOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long contentBoardId = command.contentBoardId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopContentBoard shopContentBoard = loadOwnedContentBoard(shopId, contentBoardId);
        String previousValue = describeContentBoard(shopContentBoard);

        shopContentBoardRepository.deleteById(contentBoardId);

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

    private ShopContentBoard loadOwnedContentBoard(Long shopId, Long contentBoardId) {
        ShopContentBoard shopContentBoard = shopContentBoardRepository.findById(contentBoardId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND));
        if (!shopContentBoard.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_CONTENT_BOARD_NOT_FOUND);
        }
        return shopContentBoard;
    }

    private UploadedFileId uploadIfImage(ShopContentType contentType, MultipartFile file) {
        if (contentType == ShopContentType.VIDEO) {
            return null;
        }
        shopImageSpecValidator.validateContentImage(file, contentType == ShopContentType.GIF);
        return UploadedFileId.of(fileUploadCommandService.upload(file));
    }
}
