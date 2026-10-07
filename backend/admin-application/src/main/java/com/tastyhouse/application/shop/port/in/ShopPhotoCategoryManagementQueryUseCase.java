package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;

public interface ShopPhotoCategoryManagementQueryUseCase {

    List<ShopPhotoCategoryResult> getPhotoCategories(Long id);
}
