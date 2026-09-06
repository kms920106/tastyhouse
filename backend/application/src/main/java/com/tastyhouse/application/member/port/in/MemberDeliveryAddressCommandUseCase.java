package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface MemberDeliveryAddressCommandUseCase {

    Long createDeliveryAddress(MemberDeliveryAddressCreateCommand command);

    void updateDeliveryAddress(MemberDeliveryAddressUpdateCommand command);

    void deleteDeliveryAddress(MemberDeliveryAddressDeleteCommand command);

    void changeDefaultDeliveryAddress(MemberDeliveryAddressChangeDefaultCommand command);
}
