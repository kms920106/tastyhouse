package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ShopAmenityAssignUseCase {

    Long assignAmenity(ShopAmenityManagementAssignCommand command);
}
