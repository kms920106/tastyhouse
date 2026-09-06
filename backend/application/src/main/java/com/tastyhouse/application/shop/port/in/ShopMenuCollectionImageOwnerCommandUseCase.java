package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ShopMenuCollectionImageOwnerCommandUseCase {

    Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file);

    void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command);

    void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command);
}
