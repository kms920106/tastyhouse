package com.tastyhouse.application.shop.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.port.in.ShopNoticeCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeSavePort;

@Service
@Transactional
class ShopNoticeOwnerCreateService implements ShopNoticeOwnerCreateUseCase {

    private static final int MAX_NOTICE_IMAGE_COUNT = 3;

    private final ShopNoticeSavePort shopNoticeSavePort;
    private final ShopNoticeImageSavePort shopNoticeImageSavePort;
    private final ShopNoticeExposureService shopNoticeExposureService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeOwnerCreateService(
        ShopNoticeSavePort shopNoticeSavePort,
        ShopNoticeImageSavePort shopNoticeImageSavePort,
        ShopNoticeExposureService shopNoticeExposureService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        ProhibitedWordValidator prohibitedWordValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeSavePort = shopNoticeSavePort;
        this.shopNoticeImageSavePort = shopNoticeImageSavePort;
        this.shopNoticeExposureService = shopNoticeExposureService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    @Override
    public Long createNotice(ShopNoticeCreateCommand command, List<MultipartFile> files) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String content = command.content();
        Boolean exposed = command.exposed();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(content);

        List<MultipartFile> images = normalizeFiles(files);
        validateImageCount(images);

        ShopNotice saved = shopNoticeSavePort.save(ShopNotice.of(ShopId.of(shopId), content));
        saveImages(saved.getId(), images);

        if (Boolean.TRUE.equals(exposed)) {
            shopNoticeExposureService.expose(ShopId.of(shopId), saved);
        }

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.CREATE,
            ShopChangeActor.ceo(ceoId),
            null,
            describeNotice(saved)
        );
        return saved.getId();
    }

    private String describeNotice(ShopNotice notice) {
        String label = notice.isExposed() ? "노출중" : "미노출";
        return label + ": " + notice.getContent();
    }

    private void saveImages(Long noticeId, List<MultipartFile> images) {
        if (images.isEmpty()) {
            return;
        }

        images.forEach(shopImageSpecValidator::validateNoticeImage);

        List<ShopNoticeImage> noticeImages = new ArrayList<>(images.size());
        for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
            MultipartFile file = images.get(sortOrder);
            noticeImages.add(ShopNoticeImage.of(noticeId, UploadedFileId.of(fileOwnerUploadUseCase.upload(file)), sortOrder));
        }
        shopNoticeImageSavePort.saveAll(noticeImages);
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images.size() > MAX_NOTICE_IMAGE_COUNT) {
            throw new ApplicationException(CeoErrorCode.SHOP_NOTICE_IMAGE_LIMIT_EXCEEDED);
        }
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
            .filter(file -> file != null && !file.isEmpty())
            .toList();
    }
}
