package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopIntroductionQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

/**
 * 점주용 가게소개(사장님 한마디) 조회·사전검증 서비스(CQRS query 측).
 *
 * <p>사전검증({@link #validateIntroduction})은 저장 없이 금칙어 위반 목록만 돌려주는 읽기 연산이므로
 * query 측에 둔다. 최신 사장님 한마디는 표현 목적 조회이므로 infra query DAO에서 Result를 받아 조립한다.
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopIntroductionQueryService implements ShopIntroductionQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public String getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageResult::message)
            .orElse(null);
    }

    @Override
    public ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return new ShopIntroductionValidationResult(violations.isEmpty(), violations);
    }
}
