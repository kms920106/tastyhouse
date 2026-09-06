package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaAdjustmentCreateCommand;

@Service
@CeoApp
@Transactional
public class ShopDeliveryAreaAdjustmentOwnerCommandService implements ShopDeliveryAreaAdjustmentOwnerCommandUseCase {

    private final ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopDeliveryAreaAdjustmentOwnerCommandService(
        ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService,
        ShopOwnershipValidator shopOwnershipValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.shopDeliveryAreaAdjustmentService = shopDeliveryAreaAdjustmentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.fileUploadCommandService = fileUploadCommandService;
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

        Long consentFileId = fileUploadCommandService.upload(file);

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
