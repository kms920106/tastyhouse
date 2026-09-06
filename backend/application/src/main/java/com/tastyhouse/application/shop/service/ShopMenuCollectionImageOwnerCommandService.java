package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.service.ShopMenuCollectionImageService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.file.service.FileUploadOwnerCommandService;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageReorderCommand;

@Service
@CeoApp
@Transactional
public class ShopMenuCollectionImageOwnerCommandService implements ShopMenuCollectionImageOwnerCommandUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator;
    private final FileUploadOwnerCommandService fileUploadCommandService;

    public ShopMenuCollectionImageOwnerCommandService(
        ShopMenuCollectionImageService shopMenuCollectionImageService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator,
        FileUploadOwnerCommandService fileUploadCommandService
    ) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopMenuCollectionImageSpecValidator = shopMenuCollectionImageSpecValidator;
        this.fileUploadCommandService = fileUploadCommandService;
    }

    @Override
    public Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopMenuCollectionImageSpecValidator.validate(file);

        Long imageFileId = fileUploadCommandService.upload(file);
        ShopId id = ShopId.of(shopId);
        return shopMenuCollectionImageService.register(id, UploadedFileId.of(imageFileId));
    }

    @Override
    public void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> imageIds = command.imageIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        shopMenuCollectionImageService.reorder(id, imageIds);
    }

    @Override
    public void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        ShopMenuCollectionImageId menuCollectionImageId = ShopMenuCollectionImageId.of(imageId);
        shopMenuCollectionImageService.delete(id, menuCollectionImageId);
    }
}
