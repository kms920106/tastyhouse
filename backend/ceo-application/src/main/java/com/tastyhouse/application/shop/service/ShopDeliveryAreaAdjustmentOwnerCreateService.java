package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentOwnerCreateUseCase;

@Service
@Transactional
class ShopDeliveryAreaAdjustmentOwnerCreateService implements ShopDeliveryAreaAdjustmentOwnerCreateUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;

    public ShopDeliveryAreaAdjustmentOwnerCreateService(
        ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService,
        ShopOwnershipValidator shopOwnershipValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase
    ) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
    }

    @Override
    public Long requestAdjustment(ShopDeliveryAreaAdjustmentCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String counterpartShopName = command.counterpartShopName();
        String counterpartBusinessNumber = command.counterpartBusinessNumber();
        String franchiseName = command.franchiseName();
        String reason = command.reason();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        Long consentFileId = fileOwnerUploadUseCase.upload(file);

        ShopId targetShopId = ShopId.of(shopId);
        UploadedFileId targetConsentFileId = UploadedFileId.of(consentFileId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopDeliveryAreaAdjustmentService.request(
            targetShopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            targetConsentFileId,
            actor
        );
    }
}
