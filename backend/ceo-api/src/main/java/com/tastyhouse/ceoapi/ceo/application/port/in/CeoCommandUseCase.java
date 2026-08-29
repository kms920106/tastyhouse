package com.tastyhouse.ceoapi.ceo.application.port.in;

/**
 * 점주 계정 쓰기 인바운드 포트.
 *
 * <p>호출부는 이 인터페이스만 주입하고 구현({@code CeoCommandService})을 알지 않는다.
 */
public interface CeoCommandUseCase {

    void createCeo(CeoCreateCommand command);
}
