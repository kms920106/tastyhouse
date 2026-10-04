package com.tastyhouse.application.product.port.in;

import java.util.List;

public interface ProductRepresentativeCommandUseCase {

    List<Long> requestRepresentative(ProductRepresentativeRequestCommand command);

    void clearRepresentative(ProductRepresentativeClearCommand command);
}
