package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;

public interface ShopMenuCollectionImageOwnerQueryUseCase {

    List<ShopMenuCollectionImageResult> getMenuCollectionImages(Long ceoId, Long shopId);
}
