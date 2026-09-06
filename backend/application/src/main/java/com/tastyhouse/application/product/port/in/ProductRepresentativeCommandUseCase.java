package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

@CeoApp
public interface ProductRepresentativeCommandUseCase {

    List<Long> requestRepresentative(ProductRepresentativeRequestCommand command);

    void clearRepresentative(ProductRepresentativeClearCommand command);
}
