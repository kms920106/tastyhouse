package com.tastyhouse.application.product.port.in;

import java.util.List;

public interface ProductRepresentativeRequestUseCase {

    List<Long> requestRepresentative(ProductRepresentativeRequestCommand command);
}
