package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductRepresentativeCommandUseCase {

    List<Long> requestRepresentative(ProductRepresentativeRequestCommand command);

    void clearRepresentative(ProductRepresentativeClearCommand command);
}
