package com.tastyhouse.application.product.store;

import java.util.List;

import com.tastyhouse.application.product.port.out.write.ProductAllergenStatePort;
import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductAllergenStore implements ProductAllergenRepository {
    private final ProductAllergenStatePort productAllergenStatePort;

    public ProductAllergenStore(ProductAllergenStatePort productAllergenStatePort) {
        this.productAllergenStatePort = productAllergenStatePort;
    }

    @Override
    public List<ProductAllergen> findAllByProductId(ProductId productId) {
        return productAllergenStatePort.findAllByProductId(productId == null ? null : productId.value()).stream()
            .map(ProductAllergenStateMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens) {
        return productAllergenStatePort.saveAll(productAllergens.stream().map(ProductAllergenStateMapper::toState).toList())
            .stream()
            .map(ProductAllergenStateMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        productAllergenStatePort.deleteAllByProductId(productId == null ? null : productId.value());
    }
}
