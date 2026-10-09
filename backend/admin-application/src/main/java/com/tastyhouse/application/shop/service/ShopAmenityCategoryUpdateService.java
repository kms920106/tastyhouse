package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopAmenityLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopAmenitySavePort;

@Service
@Transactional
class ShopAmenityCategoryUpdateService implements ShopAmenityCategoryUpdateUseCase {

    private final ShopAmenityLoadPort shopAmenityLoadPort;
    private final ShopAmenitySavePort shopAmenitySavePort;

    public ShopAmenityCategoryUpdateService(ShopAmenityLoadPort shopAmenityLoadPort, ShopAmenitySavePort shopAmenitySavePort) {
        this.shopAmenityLoadPort = shopAmenityLoadPort;
        this.shopAmenitySavePort = shopAmenitySavePort;
    }

    @Override
    public void updateAmenityCategory(ShopAmenityCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopAmenityCategory amenityCategory = shopAmenityLoadPort.findAmenityCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        amenityCategory.update(
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        shopAmenitySavePort.saveAmenityCategory(amenityCategory);
    }
}
