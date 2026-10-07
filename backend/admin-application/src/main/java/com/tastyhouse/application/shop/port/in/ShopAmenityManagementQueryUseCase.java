package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;

public interface ShopAmenityManagementQueryUseCase {

    List<ShopAmenityAssignmentResult> getShopAmenities(Long id);
}
