package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductImageStatePort {
    Long findRepresentativeImageFileId(Long productId);

    ProductImageState save(ProductImageState productImage);

    Optional<ProductImageState> findById(Long id);

    List<ProductImageState> findAllByProductId(Long productId);

    void deleteById(Long id);
}
