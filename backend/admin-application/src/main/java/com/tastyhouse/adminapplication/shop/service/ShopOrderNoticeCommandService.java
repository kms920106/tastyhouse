package com.tastyhouse.adminapplication.shop.service;

import com.tastyhouse.adminapplication.shop.port.in.ShopOrderNoticeCommandUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderNoticeHideCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopOrderNoticeUnhideCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopOrderNoticeService;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * admin용 주문안내 검수 변경 서비스(CQRS command 측).
 *
 * <p>주문안내는 승인 절차가 없어 점주가 저장하면 즉시 손님에게 보인다. 따라서 관리자의 개입은
 * 승인/반려가 아니라 <b>사후 게시중단</b>뿐이다 — PDF의 등록 금지 기준(전화주문 유도·계좌이체 유도·
 * 음란·정치·비방·SNS 홍보·외부링크·외부 결제 유도 등)에 걸리면 내린다.
 *
 * <p><b>금지어 자동 판정은 하지 않는다.</b> 그 기준들은 문구 전체의 맥락으로 판단해야 하고, 오탐이
 * 점주 영업을 막는 비용이 수동 검수 비용보다 크다. PDF도 자동 차단이 아니라 "수정 요청 및 삭제
 * 조치"라는 사후 조치로 규정한다.
 *
 * <p>경로에 {@code shopId}가 있고 {@code noticeId}가 없다 — 주문안내는 가게당 1건이므로 가게 하나로
 * 대상이 유일하게 특정된다({@code ShopNoticeAdminApiController}가 전역 {@code noticeId}로 경로를
 * 평탄화한 것과 다른 이유이며, 여기서는 평탄화할 대상 자체가 없다).
 *
 * <p>{@code content}는 건드리지 않는다 — 관리자는 점주 문구를 대신 고치지 않고 내리기만 한다. 게시중단
 * 해제 시 점주가 마지막으로 저장한 문구가 그대로 복원된다.
 */
@Service
@Transactional
public class ShopOrderNoticeCommandService implements ShopOrderNoticeCommandUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;

    public ShopOrderNoticeCommandService(ShopOrderNoticeService shopOrderNoticeService) {
        this.shopOrderNoticeService = shopOrderNoticeService;
    }

    /**
     * 규정을 위반한 주문안내를 게시중단한다. 사유는 {@code hidden_reason}에 남아 점주 조회로 내려간다.
     */
    @Override
    public void hideOrderNotice(ShopOrderNoticeHideCommand command) {
        Long shopId = command.shopId();
        String reason = command.reason();
        shopOrderNoticeService.hide(ShopId.of(shopId), reason);
    }

    /**
     * 게시중단된 주문안내를 다시 게시한다. 사유도 함께 비워진다.
     */
    @Override
    public void unhideOrderNotice(ShopOrderNoticeUnhideCommand command) {
        Long shopId = command.shopId();
        shopOrderNoticeService.unhide(ShopId.of(shopId));
    }
}
