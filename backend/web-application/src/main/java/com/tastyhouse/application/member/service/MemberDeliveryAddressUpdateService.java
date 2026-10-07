package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressUpdateUseCase;

@Service
@Transactional
class MemberDeliveryAddressUpdateService implements MemberDeliveryAddressUpdateUseCase {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressUpdateService(MemberDeliveryAddressService memberDeliveryAddressService) {
        this.memberDeliveryAddressService = memberDeliveryAddressService;
    }

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
}
