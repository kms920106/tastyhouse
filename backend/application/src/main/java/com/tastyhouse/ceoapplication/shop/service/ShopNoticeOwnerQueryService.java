package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopNoticeOwnerQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.port.out.ShopNoticeOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

/**
 * 점주용 가게 공지 조회 서비스(CQRS query 측).
 *
 * <p>금칙어 사전 검증도 여기에 둔다 — 저장 없이 위반 단어 목록만 돌려주는 읽기 연산이며,
 * {@code ShopIntroductionQueryService#validateIntroduction}이 선례다. 실제 저장 시점의
 * {@code SHOP_TEXT_PROHIBITED_WORD} 예외는 command 측에서만 발생한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopNoticeOwnerQueryService implements ShopNoticeOwnerQueryUseCase {

    private final ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopNoticeOwnerQueryService(
        ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopNoticeOwnerQueryPort = shopNoticeOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public List<ShopNoticeResult> getNotices(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopNoticeOwnerQueryPort.findNotices(shopId);
    }

    /**
     * 등록·수정 전 본문의 금칙어 위반 단어 목록을 돌려준다(위반이 없으면 빈 목록). 예외를 던지지 않는다.
     */
    @Override
    public List<String> validateNotice(Long ceoId, Long shopId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return prohibitedWordValidator.findViolations(content);
    }

}
