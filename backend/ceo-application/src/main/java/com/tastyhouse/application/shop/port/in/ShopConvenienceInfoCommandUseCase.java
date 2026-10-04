package com.tastyhouse.application.shop.port.in;

public interface ShopConvenienceInfoCommandUseCase {

    void updateConvenienceInfo(ShopConvenienceInfoUpdateCommand command);

    Long assignAmenity(ShopAmenityOwnerAssignCommand command);

    void unassignAmenity(ShopAmenityOwnerUnassignCommand command);
}
