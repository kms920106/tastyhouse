package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;

public interface ShopAmenityCategoryManagementQueryUseCase {

    List<ShopAmenityCategoryResult> getAmenityCategories();
}
