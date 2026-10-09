package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopBannerImageSavePort;

@Service
@Transactional
class ShopBannerImageCreateService implements ShopBannerImageCreateUseCase {

    private final ShopBannerImageSavePort shopBannerImageSavePort;

    public ShopBannerImageCreateService(ShopBannerImageSavePort shopBannerImageSavePort) {
        this.shopBannerImageSavePort = shopBannerImageSavePort;
    }

    @Override
    public Long createBannerImage(ShopBannerImageCreateCommand command) {
        Long id = command.shopId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();

        ShopBannerImage bannerImage = shopBannerImageSavePort.saveBannerImage(
            ShopBannerImage.of(ShopId.of(id), UploadedFileId.of(imageFileId), sort)
        );
        return bannerImage.getId();
    }
}
