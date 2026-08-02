package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.product.domain.vo.ProductCategoryId;

@Converter
public class ProductCategoryIdConverter implements AttributeConverter<ProductCategoryId, Long> {

    @Override
    public Long convertToDatabaseColumn(ProductCategoryId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductCategoryId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : ProductCategoryId.of(dbData);
    }
}
