package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopTrademarkCommandUseCase {

    Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file);

    Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file);
}
