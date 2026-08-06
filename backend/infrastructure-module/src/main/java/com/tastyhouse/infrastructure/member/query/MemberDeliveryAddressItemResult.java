package com.tastyhouse.infrastructure.member.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 회원 배달 주소록 목록 항목 read model.
 *
 * <p>{@code regionName}은 {@code ADMIN_DONG}을 left join해 {@code "서울특별시 강남구 역삼1동"} 형태로
 * DAO가 완성한다. 행정동 매칭에 실패한 주소({@code admin_dong_id}가 null)는 이 값이 null이다 —
 * 프론트가 시/도·시/군/구·행정동을 조립하지 않도록 서버가 표현용 문자열까지 만들어 내려준다.
 */
public record MemberDeliveryAddressItemResult(
    Long id,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    Long adminDongId,
    String regionName,
    BigDecimal latitude,
    BigDecimal longitude,
    boolean defaultAddress
) {

    @QueryProjection
    public MemberDeliveryAddressItemResult {
    }
}
