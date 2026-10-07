package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopDeliveryAreaAdjustmentOwnerCreateUseCase {

    Long requestAdjustment(ShopDeliveryAreaAdjustmentCreateCommand command, MultipartFile file);
}
