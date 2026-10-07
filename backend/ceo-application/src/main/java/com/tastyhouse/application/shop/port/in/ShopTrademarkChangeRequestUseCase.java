package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopTrademarkChangeRequestUseCase {

    Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file);
}
