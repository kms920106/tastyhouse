package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;

public interface ProductRepresentativeRequestSavePort {

    ProductRepresentativeRequest save(ProductRepresentativeRequest request);
}
