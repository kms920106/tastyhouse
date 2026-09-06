package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ShopDeliveryAreaAdjustmentOwnerCommandUseCase {

    Long requestAdjustment(ShopDeliveryAreaAdjustmentCreateCommand command, MultipartFile file);
}
