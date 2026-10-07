package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;

public interface ShopAmenityOwnerListQueryUseCase {

    List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId);
}
