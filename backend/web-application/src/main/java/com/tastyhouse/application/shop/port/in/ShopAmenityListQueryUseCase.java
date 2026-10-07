package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;

public interface ShopAmenityListQueryUseCase {

    List<ShopAmenityCategoryResult> searchAllAmenities();
}
