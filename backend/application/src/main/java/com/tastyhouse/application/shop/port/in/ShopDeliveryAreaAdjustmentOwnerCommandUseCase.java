package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopDeliveryAreaAdjustmentOwnerCommandUseCase {

    Long requestAdjustment(ShopDeliveryAreaAdjustmentCreateCommand command, MultipartFile file);
}
