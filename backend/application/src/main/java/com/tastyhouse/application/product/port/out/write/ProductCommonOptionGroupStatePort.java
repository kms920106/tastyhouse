package com.tastyhouse.application.product.port.out.write;

import java.util.List;

public interface ProductCommonOptionGroupStatePort {
    ProductCommonOptionGroupState save(ProductCommonOptionGroupState productCommonOptionGroup);

    List<ProductCommonOptionGroupState> findAllByIdIn(List<Long> ids);
}
