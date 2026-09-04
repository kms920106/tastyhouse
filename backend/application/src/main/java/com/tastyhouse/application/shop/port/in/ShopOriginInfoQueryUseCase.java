package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

/**
 * 가게 원산지 정보 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopOriginInfoQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface ShopOriginInfoQueryUseCase {

    /** 미설정이면 {@code null} — 손님 화면은 원산지 영역을 통째로 감춘다. */
    ShopOriginInfoResult getOriginInfo(Long shopId);
}
