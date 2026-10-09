package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;

public interface ShopPhotoCategorySavePort {

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);
}
