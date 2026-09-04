package com.tastyhouse.application.member.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달 주소 등록 command.
 *
 * <p>주소 길이·좌표 필수 검증은 Request의 jakarta.validation이 담당하고, 이 record는 필수값 누락 같은
 * 구조적 가드만 둔다.
 *
 * <p><b>이 리포에서 위치 기반 뒤바뀜 위험이 가장 큰 command 중 하나다</b> — 주소 {@code String} 4개
 * ({@code alias}·{@code roadAddress}·{@code lotAddress}·{@code detailAddress})와 좌표
 * {@code BigDecimal} 2개({@code latitude}·{@code longitude})가 각각 연달아 있어, 순서가 어긋나도
 * 컴파일은 통과하고 값만 조용히 뒤바뀐다. 위경도가 뒤바뀌면 거리별 배달팁이 엉뚱하게 산출된다.
 *
 * <p><b>필드 선언 순서는 {@code MemberDeliveryAddressService#create}의 인자 순서와 일치시켰다</b> —
 * 서비스가 이 command를 위치 기반으로 도메인 서비스에 전달하기 때문이다. 한쪽을 고치면 반드시 다른
 * 쪽도 함께 고친다. {@code toCommand}는 이름 기반 접근자로 각 값을 짚어 넘긴다.
 *
 * <p>{@code isDefault}는 하위호환을 위해 null을 허용한다(서비스가 {@code Boolean.TRUE.equals}로 정규화).
 */
public record MemberDeliveryAddressCreateCommand(
    Long memberId,
    String alias,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean isDefault
) {
    public MemberDeliveryAddressCreateCommand {
        if (memberId == null || roadAddress == null || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
