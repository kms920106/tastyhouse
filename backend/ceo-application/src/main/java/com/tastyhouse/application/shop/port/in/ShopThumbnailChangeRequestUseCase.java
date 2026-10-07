package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopThumbnailChangeRequestUseCase {

    Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file);
}
