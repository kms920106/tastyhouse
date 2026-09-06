package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ShopTrademarkCommandUseCase {

    Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file);

    Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file);
}
