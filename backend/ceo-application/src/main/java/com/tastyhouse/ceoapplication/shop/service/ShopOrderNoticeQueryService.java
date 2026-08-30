package com.tastyhouse.ceoapplication.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopOrderNoticeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.ceoapplication.shop.response.ShopOrderNoticeResponse;

/**
 * 점주용 주문안내 조회 서비스(CQRS query 측).
 *
 * <p>게시중단된 문구도 그대로 내려준다 — 점주는 자기 문구가 내려갔다는 사실과 그 사유를 알아야
 * 고칠 수 있다. 손님 경로(web-api)는 반대로 게시중단 건을 아예 받지 않으며, 그 필터는 Service가
 * 아니라 DAO의 별도 조회 메서드가 강제한다({@code ShopOrderNoticeManagementQueryPort} 참조).
 *
 * <p>CQRS 교차 주입 금지 규칙에 따라 write 포트({@code ShopOrderNoticeRepository})를 주입하지 않는다.
 * 소유권 검증은 write 포트를 내부에 감춘 협력 빈 {@link ShopOwnershipValidator}를 경유한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopOrderNoticeQueryService implements ShopOrderNoticeQueryUseCase {

    private final ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOrderNoticeQueryService(
        ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOrderNoticeManagementQueryPort = shopOrderNoticeManagementQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 가게의 주문안내를 조회한다. 미설정이면 {@code content}가 null인 빈 응답을 돌려준다
     * (사유는 {@code ShopOrderNoticeResponse#empty} 참조).
     */
    @Override
    public ShopOrderNoticeResponse getOrderNotice(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopOrderNoticeManagementQueryPort.findOrderNotice(shopId)
            .map(result -> ShopOrderNoticeResponse.of(result.content(), result.hidden(), result.hiddenReason()))
            .orElseGet(ShopOrderNoticeResponse::empty);
    }
}
