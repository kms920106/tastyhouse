package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.vo.ProductOptionId;

@Converter
public class ProductOptionIdConverter implements AttributeConverter<ProductOptionId, Long> {

    @Override
    public Long convertToDatabaseColumn(ProductOptionId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductOptionId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ProductOptionId.of(dbData);
    }
}
