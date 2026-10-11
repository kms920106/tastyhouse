package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;

public interface ProductCommonOptionGroupLinkSavePort {

    ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link);

    void delete(ProductCommonOptionGroupLink link);
}
