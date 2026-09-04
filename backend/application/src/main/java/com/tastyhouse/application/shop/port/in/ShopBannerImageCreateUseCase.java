package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/**
 * 배너 이미지 쓰기 인바운드 포트(admin).
 *
 * <p>{@code ShopCommandService}는 public 메서드가 37개라 하위 자원별 연산 단위 인터페이스로 분해했다
 * (챕터 02 §4 per-operation 분해 기준: 7개 초과). 서비스는 이 인터페이스들을 모두 implements한다.
 */
@AdminApp
public interface ShopBannerImageCreateUseCase {

    Long createBannerImage(ShopBannerImageCreateCommand command);
}
