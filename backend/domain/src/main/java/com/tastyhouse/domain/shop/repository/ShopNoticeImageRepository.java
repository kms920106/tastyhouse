package com.tastyhouse.domain.shop.repository;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopNoticeImage;

public interface ShopNoticeImageRepository {
    void saveAll(List<ShopNoticeImage> images);

    void deleteByShopNoticeId(Long shopNoticeId);
}
