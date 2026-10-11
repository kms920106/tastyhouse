package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;

public interface ShopPhotoCategoryLoadPort {

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);
}
