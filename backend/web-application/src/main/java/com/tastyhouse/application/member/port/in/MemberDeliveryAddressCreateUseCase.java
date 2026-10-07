package com.tastyhouse.application.member.port.in;

public interface MemberDeliveryAddressCreateUseCase {

    Long createDeliveryAddress(MemberDeliveryAddressCreateCommand command);
}
