package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopAmenitySavePort;

@Service
@Transactional
class ShopAmenityCategoryCreateService implements ShopAmenityCategoryCreateUseCase {

    private final ShopAmenitySavePort shopAmenitySavePort;

    public ShopAmenityCategoryCreateService(ShopAmenitySavePort shopAmenitySavePort) {
        this.shopAmenitySavePort = shopAmenitySavePort;
    }

    @Override
    public Long createAmenityCategory(ShopAmenityCategoryCreateCommand command) {
        String amenity = command.amenity();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopAmenityCategory amenityCategory = ShopAmenityCategory.of(
            Amenity.from(amenity),
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        return shopAmenitySavePort.saveAmenityCategory(amenityCategory).getId();
    }
}
