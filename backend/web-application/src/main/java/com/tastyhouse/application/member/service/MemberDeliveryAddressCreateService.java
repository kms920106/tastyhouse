package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCreateCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCreateUseCase;

@Service
@Transactional
class MemberDeliveryAddressCreateService implements MemberDeliveryAddressCreateUseCase {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressCreateService(MemberDeliveryAddressService memberDeliveryAddressService) {
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
}
