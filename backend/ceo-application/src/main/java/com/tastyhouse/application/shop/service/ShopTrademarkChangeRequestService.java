package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shop.port.in.ShopTrademarkChangeRequestCommand;
import com.tastyhouse.application.shop.port.in.ShopTrademarkChangeRequestUseCase;

@Service
@Transactional
class ShopTrademarkChangeRequestService implements ShopTrademarkChangeRequestUseCase {

    private final ShopImageApprovalService shopImageApprovalService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopImageSpecValidator shopImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;

    public ShopTrademarkChangeRequestService(
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
    public Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopImageSpecValidator.validateTrademark(file);

        Long imageFileId = fileOwnerUploadUseCase.upload(file);
        return shopImageApprovalService.requestImageChange(
            shopId, ShopImageType.TRADEMARK, imageFileId, ShopChangeActor.ceo(ceoId)
        );
    }
}
