package com.tastyhouse.application.product.port.out;

import java.util.List;

import com.tastyhouse.domain.product.model.VegetarianType;

public record ProductVegetarianStatusResult(
    VegetarianType vegetarianType,
    List<ProductVegetarianRequestResult> requests,
    boolean changeable
) {
}
