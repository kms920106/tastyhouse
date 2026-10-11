package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;

public interface ProductOptionGroupLinkSavePort {

    ProductOptionGroupLink save(ProductOptionGroupLink link);

    void delete(ProductOptionGroupLink link);
}
