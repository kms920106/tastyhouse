package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopTrademarkCommandUseCase {

    Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file);

    Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file);
}
