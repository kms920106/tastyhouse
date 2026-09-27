package com.tastyhouse.application.product.port.out;

import java.util.List;

public record ProductVegetarianStatusResult(
    String vegetarianType,
    List<ProductVegetarianRequestResult> requests,
    boolean changeable
) {
}
