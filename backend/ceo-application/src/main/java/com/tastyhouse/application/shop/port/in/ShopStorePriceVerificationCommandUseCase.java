package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopStorePriceVerificationCommandUseCase {

    Long requestVerification(ShopStorePriceVerificationRequestCommand command, MultipartFile file);
}
