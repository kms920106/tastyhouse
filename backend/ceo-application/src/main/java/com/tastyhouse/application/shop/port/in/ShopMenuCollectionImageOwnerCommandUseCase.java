package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopMenuCollectionImageOwnerCommandUseCase {

    Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file);

    void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command);

    void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command);
}
