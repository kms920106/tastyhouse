package com.tastyhouse.webapi.member.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 회원 배달 주소록 명령 서비스.
 *
 * <p>기본 배송지 유일성·회원당 10건 한도·소유권 검증·행정동 매칭은 전부 도메인 서비스
 * ({@link MemberDeliveryAddressService})가 소유한다. 이 서비스는 트랜잭션 경계와
 * {@code MemberId.of} 승격만 담당한다({@code MemberCommandService}와 동일 구조).
 *
 * <p>CQRS 교차 주입 금지 — 조회는 {@code MemberDeliveryAddressQueryService}가 담당하며 이 서비스는
 * {@code ..query..}를 주입하지 않는다.
 */
@Service
@Transactional
public class MemberDeliveryAddressCommandService {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressCommandService(MemberDeliveryAddressService memberDeliveryAddressService) {
        this.memberDeliveryAddressService = memberDeliveryAddressService;
    }

    public Long createDeliveryAddress(
        Long memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean isDefault
    ) {
        MemberId id = MemberId.of(memberId);
        return memberDeliveryAddressService.create(
            id,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            latitude,
            longitude,
            Boolean.TRUE.equals(isDefault)
        );
    }

    /**
     * 배달 주소를 수정한다.
     *
     * <p><b>기본 배송지 변경은 이 경로가 담당하지 않는다</b> — {@code PATCH
     * /v1/me/delivery-addresses/{id}/default}가 단독 소유한다. 수정 요청에 {@code isDefault}를 두면
     * 서버가 받고도 무시하는 필드가 되거나, 기본 배송지 유일성 규칙이 두 경로로 갈린다.
     */
    public void updateDeliveryAddress(
        Long memberId,
        Long addressId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        MemberId id = MemberId.of(memberId);
        memberDeliveryAddressService.update(
            id,
            addressId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            latitude,
            longitude
        );
    }

    public void deleteDeliveryAddress(Long memberId, Long addressId) {
        MemberId id = MemberId.of(memberId);
        memberDeliveryAddressService.delete(id, addressId);
    }

    public void changeDefaultDeliveryAddress(Long memberId, Long addressId) {
        MemberId id = MemberId.of(memberId);
        memberDeliveryAddressService.changeDefault(id, addressId);
    }
}
