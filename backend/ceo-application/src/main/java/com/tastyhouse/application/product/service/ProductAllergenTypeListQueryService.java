package com.tastyhouse.application.product.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.AllergenType;
import com.tastyhouse.application.product.port.in.ProductAllergenTypeListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductAllergenTypeView;

@Service
@Transactional(readOnly = true)
class ProductAllergenTypeListQueryService implements ProductAllergenTypeListQueryUseCase {

    @Override
    public List<ProductAllergenTypeView> getAllergenTypes() {
        return Arrays.stream(AllergenType.values())
            .map(allergenType -> new ProductAllergenTypeView(allergenType.name(), allergenType.getDescription()))
            .toList();
    }
}
