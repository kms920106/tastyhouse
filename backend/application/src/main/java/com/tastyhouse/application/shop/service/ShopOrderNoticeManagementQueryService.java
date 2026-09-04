package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeManagementQueryUseCase;

/**
 * 관리자용 주문안내 조회 서비스(CQRS query 측).
 *
 * <p>게시중단 여부·사유를 그대로 내려준다 — 관리자가 게시중단/해제를 판단하려면 현재 문구와 사유를
 * 먼저 봐야 한다. 소유권 검증은 하지 않는다(관리자는 전 가게에 접근 가능).
 *
 * <p>CQRS 교차 주입 금지 규칙에 따라 write 포트를 주입하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 미등록 가게의 빈 응답 조립도 표현 계약이라 컨트롤러가 담당한다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopOrderNoticeManagementQueryService implements ShopOrderNoticeManagementQueryUseCase {

    private final ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort;

    public ShopOrderNoticeManagementQueryService(ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort) {
        this.shopOrderNoticeManagementQueryPort = shopOrderNoticeManagementQueryPort;
    }

    /**
     * 가게의 주문안내를 조회한다. 미설정이면 빈 {@code Optional}이다.
     */
    @Override
    public Optional<ShopOrderNoticeResult> getOrderNotice(Long shopId) {
        return shopOrderNoticeManagementQueryPort.findOrderNotice(shopId);
    }
}
