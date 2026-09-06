package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;

@CeoApp
public interface ShopConvenienceInfoQueryUseCase {

    Optional<ShopConvenienceInfoResult> getConvenienceInfo(Long ceoId, Long shopId);

    List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId);
}
