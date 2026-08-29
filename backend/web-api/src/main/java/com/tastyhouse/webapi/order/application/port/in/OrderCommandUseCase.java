package com.tastyhouse.webapi.order.application.port.in;

/**
 * 회원 주문 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code OrderCommandService})을 알지 않는다.
 */
public interface OrderCommandUseCase {

    Long createOrder(OrderCreateCommand command);
}
