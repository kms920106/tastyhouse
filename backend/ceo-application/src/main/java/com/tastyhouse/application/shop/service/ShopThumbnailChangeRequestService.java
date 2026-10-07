package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shop.port.in.ShopThumbnailChangeRequestCommand;
import com.tastyhouse.application.shop.port.in.ShopThumbnailChangeRequestUseCase;

@Service
@Transactional
class ShopThumbnailChangeRequestService implements ShopThumbnailChangeRequestUseCase {

    private final ShopImageApprovalService shopImageApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;

    public ShopThumbnailChangeRequestService(
        ShopImageApprovalService shopImageApprovalService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase
    ) {
        this.shopImageApprovalService = shopImageApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
    }

    @Override
    public Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateContentImage(file, false);

        Long imageFileId = fileOwnerUploadUseCase.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.THUMBNAIL, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }
}
