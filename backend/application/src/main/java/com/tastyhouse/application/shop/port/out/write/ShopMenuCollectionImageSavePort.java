package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;

public interface ShopMenuCollectionImageSavePort {

    ShopMenuCollectionImage save(ShopMenuCollectionImage image);

    void delete(ShopMenuCollectionImage image);
}
