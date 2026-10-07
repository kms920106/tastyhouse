package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressChangeDefaultCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressChangeDefaultUseCase;

@Service
@Transactional
class MemberDeliveryAddressChangeDefaultService implements MemberDeliveryAddressChangeDefaultUseCase {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressChangeDefaultService(MemberDeliveryAddressService memberDeliveryAddressService) {
        this.memberDeliveryAddressService = memberDeliveryAddressService;
    }

    @Override
    public void changeDefaultDeliveryAddress(MemberDeliveryAddressChangeDefaultCommand command) {
        MemberId id = MemberId.of(command.memberId());
        memberDeliveryAddressService.changeDefault(id, command.addressId());
    }
}
