package com.tastyhouse.application.product.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageChangeRequestUseCase {

    Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file);
}
