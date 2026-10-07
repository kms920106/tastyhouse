package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;

public interface ShopPhotoCategoryImageManagementQueryUseCase {

    List<ShopPhotoCategoryImageManagementResult> getPhotoCategoryImages(Long categoryId);
}
