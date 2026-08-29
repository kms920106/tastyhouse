package com.tastyhouse.webapi.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressChangeDefaultCommand;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressCommandUseCase;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressCreateCommand;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressDeleteCommand;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressUpdateCommand;

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
public class MemberDeliveryAddressCommandService implements MemberDeliveryAddressCommandUseCase {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressCommandService(MemberDeliveryAddressService memberDeliveryAddressService) {
        this.memberDeliveryAddressService = memberDeliveryAddressService;
    }

    @Override
    public Long createDeliveryAddress(MemberDeliveryAddressCreateCommand command) {
        MemberId id = MemberId.of(command.memberId());
        return memberDeliveryAddressService.create(
            id,
            command.alias(),
            command.roadAddress(),
            command.lotAddress(),
            command.detailAddress(),
            command.latitude(),
            command.longitude(),
            Boolean.TRUE.equals(command.isDefault())
        );
    }

    /**
     * 배달 주소를 수정한다.
     *
     * <p><b>기본 배송지 변경은 이 경로가 담당하지 않는다</b> — {@code PATCH
     * /v1/me/delivery-addresses/{id}/default}가 단독 소유한다. 수정 요청에 {@code isDefault}를 두면
     * 서버가 받고도 무시하는 필드가 되거나, 기본 배송지 유일성 규칙이 두 경로로 갈린다.
     */
    @Override
    public void updateDeliveryAddress(MemberDeliveryAddressUpdateCommand command) {
        MemberId id = MemberId.of(command.memberId());
        memberDeliveryAddressService.update(
            id,
            command.addressId(),
            command.alias(),
            command.roadAddress(),
            command.lotAddress(),
            command.detailAddress(),
            command.latitude(),
            command.longitude()
        );
    }

    @Override
    public void deleteDeliveryAddress(MemberDeliveryAddressDeleteCommand command) {
        MemberId id = MemberId.of(command.memberId());
        memberDeliveryAddressService.delete(id, command.addressId());
    }

    @Override
    public void changeDefaultDeliveryAddress(MemberDeliveryAddressChangeDefaultCommand command) {
        MemberId id = MemberId.of(command.memberId());
        memberDeliveryAddressService.changeDefault(id, command.addressId());
    }
}
