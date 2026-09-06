package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ProductImageCommandUseCase {

    Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file);

    void reorderImages(ProductImageReorderCommand command);

    void deleteImage(ProductImageDeleteCommand command);
}
