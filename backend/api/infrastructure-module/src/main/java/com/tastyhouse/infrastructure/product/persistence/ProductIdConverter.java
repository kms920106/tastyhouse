package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.vo.ProductId;

@Converter
public class ProductIdConverter implements AttributeConverter<ProductId, Long> {

    @Override
    public Long convertToDatabaseColumn(ProductId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ProductId.of(dbData);
    }
}
