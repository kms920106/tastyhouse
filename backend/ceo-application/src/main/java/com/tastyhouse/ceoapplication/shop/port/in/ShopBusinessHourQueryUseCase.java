package com.tastyhouse.ceoapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.apicommon.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.apicommon.shop.response.ShopBusinessHourResponse;

/**
 * 가게 영업시간 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopBusinessHourQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopBusinessHourQueryUseCase {

    List<ShopBusinessHourResponse> getBusinessHours(Long ceoId, Long shopId);

    List<ShopBreakTimeResponse> getBreakTimes(Long ceoId, Long shopId);
}
