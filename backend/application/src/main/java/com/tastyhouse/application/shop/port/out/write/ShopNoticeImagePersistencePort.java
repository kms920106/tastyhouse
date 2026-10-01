package com.tastyhouse.application.shop.port.out.write;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;

public interface ShopNoticeImagePersistencePort {

    void saveAll(List<ShopNoticeImage> images);

    void deleteByShopNoticeId(Long shopNoticeId);
}
