package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopConvenienceInfoCommandUseCase {

    void updateConvenienceInfo(ShopConvenienceInfoUpdateCommand command);

    Long assignAmenity(ShopAmenityOwnerAssignCommand command);

    void unassignAmenity(ShopAmenityOwnerUnassignCommand command);
}
