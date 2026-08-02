package com.tastyhouse.infrastructure.coupon.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.coupon.vo.CouponId;

@Converter
public class CouponIdConverter implements AttributeConverter<CouponId, Long> {

    @Override
    public Long convertToDatabaseColumn(CouponId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CouponId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : CouponId.of(dbData);
    }
}
