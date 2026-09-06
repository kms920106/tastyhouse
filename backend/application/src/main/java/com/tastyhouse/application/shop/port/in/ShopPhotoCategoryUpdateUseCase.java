package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopPhotoCategoryUpdateUseCase {

    void updatePhotoCategory(ShopPhotoCategoryUpdateCommand command);
}
