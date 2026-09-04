package com.tastyhouse.application.member.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달 주소 수정 command.
 *
 * <p><b>등록 command와 같은 위치 기반 뒤바뀜 위험이 있다</b> — 주소 {@code String} 4개와 좌표
 * {@code BigDecimal} 2개가 연달아 있어 순서가 어긋나도 컴파일은 통과한다. 필드 선언 순서는
 * {@code MemberDeliveryAddressService#update}의 인자 순서와 일치시켰고, {@code toCommand}는 이름 기반
 * 접근자로 각 값을 짚어 넘긴다.
 *
 * <p>{@code isDefault}가 없는 것은 의도다 — 기본 배송지 지정은 전용 엔드포인트
 * ({@code PATCH /v1/me/delivery-addresses/{id}/default})가 단독 소유한다.
 */
public record MemberDeliveryAddressUpdateCommand(
    Long memberId,
    Long addressId,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public MemberDeliveryAddressUpdateCommand {
        if (memberId == null || addressId == null || roadAddress == null
            || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
