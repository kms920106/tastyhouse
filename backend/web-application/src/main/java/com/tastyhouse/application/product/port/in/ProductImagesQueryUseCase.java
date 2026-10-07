package com.tastyhouse.application.product.port.in;

import java.util.List;

public interface ProductImagesQueryUseCase {

    List<String> findProductImages(Long productId);
}
