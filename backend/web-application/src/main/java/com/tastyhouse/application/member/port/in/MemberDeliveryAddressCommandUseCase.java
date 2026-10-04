package com.tastyhouse.application.member.port.in;

public interface MemberDeliveryAddressCommandUseCase {

    Long createDeliveryAddress(MemberDeliveryAddressCreateCommand command);

    void updateDeliveryAddress(MemberDeliveryAddressUpdateCommand command);

    void deleteDeliveryAddress(MemberDeliveryAddressDeleteCommand command);

    void changeDefaultDeliveryAddress(MemberDeliveryAddressChangeDefaultCommand command);
}
