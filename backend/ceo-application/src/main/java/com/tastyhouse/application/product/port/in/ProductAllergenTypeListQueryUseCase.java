package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;

public interface ProductAllergenTypeListQueryUseCase {

    List<ProductAllergenTypeView> getAllergenTypes();
}
