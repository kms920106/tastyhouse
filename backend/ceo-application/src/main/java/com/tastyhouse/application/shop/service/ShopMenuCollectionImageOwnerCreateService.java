package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.file.port.in.FileOwnerUploadUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerCreateUseCase;

@Service
@Transactional
class ShopMenuCollectionImageOwnerCreateService implements ShopMenuCollectionImageOwnerCreateUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator;
    private final FileOwnerUploadUseCase fileOwnerUploadUseCase;

    public ShopMenuCollectionImageOwnerCreateService(
        ShopMenuCollectionImageService shopMenuCollectionImageService,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopMenuCollectionImageSpecValidator shopMenuCollectionImageSpecValidator,
        FileOwnerUploadUseCase fileOwnerUploadUseCase
    ) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopMenuCollectionImageSpecValidator = shopMenuCollectionImageSpecValidator;
        this.fileOwnerUploadUseCase = fileOwnerUploadUseCase;
    }

    @Override
    public Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopMenuCollectionImageSpecValidator.validate(file);

        Long imageFileId = fileOwnerUploadUseCase.upload(file);
        ShopId id = ShopId.of(shopId);
        return shopMenuCollectionImageService.register(id, UploadedFileId.of(imageFileId));
    }
}
