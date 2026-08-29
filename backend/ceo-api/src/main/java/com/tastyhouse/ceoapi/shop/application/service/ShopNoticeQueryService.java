package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopNoticeQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.application.shop.port.out.ShopNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopNoticeResponse;

/**
 * 점주용 가게 공지 조회 서비스(CQRS query 측).
 *
 * <p>금칙어 사전 검증도 여기에 둔다 — 저장 없이 위반 단어 목록만 돌려주는 읽기 연산이며,
 * {@code ShopIntroductionQueryService#validateIntroduction}이 선례다. 실제 저장 시점의
 * {@code SHOP_TEXT_PROHIBITED_WORD} 예외는 command 측에서만 발생한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopNoticeQueryService implements ShopNoticeQueryUseCase {

    private final ShopNoticeQueryPort shopNoticeQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopNoticeQueryService(
        ShopNoticeQueryPort shopNoticeQueryPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopNoticeQueryPort = shopNoticeQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public List<ShopNoticeResponse> getNotices(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopNoticeQueryPort.findNotices(shopId).stream()
            .map(this::toShopNoticeResponse)
            .toList();
    }

    /**
     * 등록·수정 전 본문의 금칙어 위반 단어 목록을 돌려준다(위반이 없으면 빈 목록). 예외를 던지지 않는다.
     */
    @Override
    public List<String> validateNotice(Long ceoId, Long shopId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return prohibitedWordValidator.findViolations(content);
    }

    private ShopNoticeResponse toShopNoticeResponse(ShopNoticeResult dto) {
        return ShopNoticeResponse.of(
            dto.id(),
            dto.content(),
            dto.imageUrls(),
            dto.exposed(),
            dto.hidden(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
