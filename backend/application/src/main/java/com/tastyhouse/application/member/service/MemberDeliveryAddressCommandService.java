package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressChangeDefaultCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressCreateCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressDeleteCommand;
import com.tastyhouse.application.member.port.in.MemberDeliveryAddressUpdateCommand;

@Service
@WebApp
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
