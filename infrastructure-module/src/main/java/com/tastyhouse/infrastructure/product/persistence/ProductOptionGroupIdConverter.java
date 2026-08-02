package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.domain.vo.ProductOptionGroupId;

@Converter
public class ProductOptionGroupIdConverter implements AttributeConverter<ProductOptionGroupId, Long> {

    @Override
    public Long convertToDatabaseColumn(ProductOptionGroupId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductOptionGroupId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ProductOptionGroupId.of(dbData);
    }
}
