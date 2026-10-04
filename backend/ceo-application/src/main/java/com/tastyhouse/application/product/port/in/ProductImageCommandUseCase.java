package com.tastyhouse.application.product.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageCommandUseCase {

    Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file);

    void reorderImages(ProductImageReorderCommand command);

    void deleteImage(ProductImageDeleteCommand command);
}
