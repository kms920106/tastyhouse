package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopAmenityCategoryCreateUseCase {

    Long createAmenityCategory(ShopAmenityCategoryCreateCommand command);
}
