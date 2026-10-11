package com.tastyhouse.application.product.port.out;

public interface ProductOptionQueryPort {

    ProductOptionsResult findProductOptions(Long productId, String commonOptionGroupType);
}
