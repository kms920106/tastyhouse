package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum VegetarianType {

    VEGAN("비건"),

    LACTO("락토"),

    OVO("오보"),

    LACTO_OVO("락토오보"),

    PESCO("페스코");

    private final String description;

    VegetarianType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static VegetarianType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException(DomainErrorCode.PRODUCT_VEGETARIAN_TYPE_UNKNOWN,
                DomainErrorCode.PRODUCT_VEGETARIAN_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
