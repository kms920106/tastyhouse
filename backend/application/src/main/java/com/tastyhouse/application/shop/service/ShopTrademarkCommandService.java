package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.service.ShopImageApprovalService;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopThumbnailChangeRequestCommand;
import com.tastyhouse.application.shop.port.in.ShopTrademarkChangeRequestCommand;
import com.tastyhouse.application.shop.port.in.ShopTrademarkCommandUseCase;

@Service
@CeoApp
@Transactional
public class ShopTrademarkCommandService implements ShopTrademarkCommandUseCase {

    private final ShopImageApprovalService shopImageApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopTrademarkCommandService(
        ShopImageApprovalService shopImageApprovalService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopImageSpecValidator shopImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.shopImageApprovalService = shopImageApprovalService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopImageSpecValidator = shopImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    @Override
    public Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateTrademark(file);

        Long imageFileId = fileUploadCommandService.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.TRADEMARK, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }

    @Override
    public Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateContentImage(file, false);

        Long imageFileId = fileUploadCommandService.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.THUMBNAIL, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }
}
