package com.tastyhouse.infrastructure.coupon.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;

@Converter
public class MemberCouponIdConverter implements AttributeConverter<MemberCouponId, Long> {

    @Override
    public Long convertToDatabaseColumn(MemberCouponId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public MemberCouponId convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : MemberCouponId.of(dbData);
    }
}
