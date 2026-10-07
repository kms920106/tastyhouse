package com.tastyhouse.application.product.port.in;

import java.util.List;

public interface ProductImageManagementListQueryUseCase {

    List<String> getProductImages(Long id);
}
