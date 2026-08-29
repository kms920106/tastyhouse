package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopNoticeResponse;

/**
 * 가게 공지 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopNoticeQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopNoticeQueryUseCase {

    List<ShopNoticeResponse> getNotices(Long ceoId, Long shopId);

    List<String> validateNotice(Long ceoId, Long shopId, String content);
}
