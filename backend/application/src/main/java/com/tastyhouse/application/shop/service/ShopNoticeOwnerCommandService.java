package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
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
import com.tastyhouse.domain.shop.repository.ShopNoticeImageRepository;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopNoticeExposureService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopNoticeCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeExposureChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopNoticeUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopNoticeOwnerCommandService implements ShopNoticeOwnerCommandUseCase {

    private static final int MAX_NOTICE_IMAGE_COUNT = 3;

    private final ShopNoticeRepository shopNoticeRepository;
    private final ShopNoticeImageRepository shopNoticeImageRepository;
    private final ShopNoticeExposureService shopNoticeExposureService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopNoticeOwnerCommandService(
        ShopNoticeRepository shopNoticeRepository,
        ShopNoticeImageRepository shopNoticeImageRepository,
        ShopNoticeExposureService shopNoticeExposureService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        ProhibitedWordValidator prohibitedWordValidator,
        FileUploadOwnerCommandService fileUploadCommandService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopNoticeRepository = shopNoticeRepository;
        this.shopNoticeImageRepository = shopNoticeImageRepository;
        this.shopNoticeExposureService = shopNoticeExposureService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.fileUploadCommandService = fileUploadCommandService;
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

        ShopNotice saved = shopNoticeRepository.save(ShopNotice.of(ShopId.of(shopId), content));
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

    @Override
    public void updateNotice(ShopNoticeUpdateCommand command, List<MultipartFile> files) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long noticeId = command.noticeId();
        String content = command.content();
        Boolean keepExistingImages = command.keepExistingImages();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        prohibitedWordValidator.validate(content);

        ShopNotice notice = loadOwnedNotice(shopId, noticeId);

        String previousValue = describeNotice(notice);

        if (!Boolean.TRUE.equals(keepExistingImages)) {
            List<MultipartFile> images = normalizeFiles(files);
            validateImageCount(images);
            shopNoticeImageRepository.deleteByShopNoticeId(noticeId);
            saveImages(noticeId, images);
        }

        notice.updateContent(content);
        shopNoticeRepository.save(notice);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeNotice(notice)
        );
    }

    @Override
    public void deleteNotice(ShopNoticeDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long noticeId = command.noticeId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        shopNoticeImageRepository.deleteByShopNoticeId(noticeId);
        shopNoticeRepository.deleteById(noticeId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.DELETE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            null
        );
    }

    @Override
    public void changeExposure(ShopNoticeExposureChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long noticeId = command.noticeId();
        boolean exposed = command.exposed();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopNotice notice = loadOwnedNotice(shopId, noticeId);
        String previousValue = describeNotice(notice);

        if (exposed) {
            shopNoticeExposureService.expose(ShopId.of(shopId), notice);
        } else {
            shopNoticeExposureService.unexpose(notice);
        }

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.NOTICE,
            ShopChangeActionType.UPDATE,
            ShopChangeActor.ceo(ceoId),
            previousValue,
            describeNotice(notice)
        );
    }

    private String describeNotice(ShopNotice notice) {
        String label = notice.isExposed() ? "노출중" : "미노출";
        return label + ": " + notice.getContent();
    }

    private ShopNotice loadOwnedNotice(Long shopId, Long noticeId) {
        ShopNotice notice = shopNoticeRepository.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND));
        if (!notice.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.SHOP_NOTICE_NOT_FOUND);
        }
        return notice;
    }

    private void saveImages(Long noticeId, List<MultipartFile> images) {
        if (images.isEmpty()) {
            return;
        }

        images.forEach(shopImageSpecValidator::validateNoticeImage);

        List<ShopNoticeImage> noticeImages = new ArrayList<>(images.size());
        for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
            MultipartFile file = images.get(sortOrder);
            noticeImages.add(ShopNoticeImage.of(noticeId, UploadedFileId.of(fileUploadCommandService.upload(file)), sortOrder));
        }
        shopNoticeImageRepository.saveAll(noticeImages);
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images.size() > MAX_NOTICE_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_NOTICE_IMAGE_LIMIT_EXCEEDED);
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
