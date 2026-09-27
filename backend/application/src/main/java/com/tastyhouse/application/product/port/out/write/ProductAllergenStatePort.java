package com.tastyhouse.application.product.port.out.write;

import java.util.List;

public interface ProductAllergenStatePort {
    List<ProductAllergenState> findAllByProductId(Long productId);

    List<ProductAllergenState> saveAll(List<ProductAllergenState> productAllergens);

    void deleteAllByProductId(Long productId);
}
