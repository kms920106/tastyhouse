package com.tastyhouse.webapi.member.application.port.in;

/**
 * 회원 배달 주소록 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MemberDeliveryAddressCommandService})을 알지 않는다.
 */
public interface MemberDeliveryAddressCommandUseCase {

    Long createDeliveryAddress(MemberDeliveryAddressCreateCommand command);

    void updateDeliveryAddress(MemberDeliveryAddressUpdateCommand command);

    void deleteDeliveryAddress(MemberDeliveryAddressDeleteCommand command);

    void changeDefaultDeliveryAddress(MemberDeliveryAddressChangeDefaultCommand command);
}
