package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopStorePriceVerificationRequestUseCase {

    Long requestVerification(ShopStorePriceVerificationRequestCommand command, MultipartFile file);
}
