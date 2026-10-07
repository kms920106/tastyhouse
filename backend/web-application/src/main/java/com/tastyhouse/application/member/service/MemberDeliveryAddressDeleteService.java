package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressDeleteCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressDeleteUseCase;

@Service
@Transactional
class MemberDeliveryAddressDeleteService implements MemberDeliveryAddressDeleteUseCase {

    private final MemberDeliveryAddressService memberDeliveryAddressService;

    public MemberDeliveryAddressDeleteService(MemberDeliveryAddressService memberDeliveryAddressService) {
        this.memberDeliveryAddressService = memberDeliveryAddressService;
    }

    @Override
    public void deleteDeliveryAddress(MemberDeliveryAddressDeleteCommand command) {
        MemberId id = MemberId.of(command.memberId());
        memberDeliveryAddressService.delete(id, command.addressId());
    }
}
