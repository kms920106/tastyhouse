package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.ceoapplication.shop.response.ShopIntroductionResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopIntroductionValidationResponse;

/**
 * 점주용 가게소개(사장님 한마디) 조회·사전검증 서비스(CQRS query 측).
 *
 * <p>사전검증({@link #validateIntroduction})은 저장 없이 금칙어 위반 목록만 돌려주는 읽기 연산이므로
 * query 측에 둔다. 최신 사장님 한마디는 표현 목적 조회이므로 infra query DAO에서 Result를 받아 조립한다.
 */
@Service
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
    public ShopIntroductionResponse getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String message = shopBasicInfoQueryPort.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageResult::message)
            .orElse(null);
        return ShopIntroductionResponse.from(message);
    }

    @Override
    public ShopIntroductionValidationResponse validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return ShopIntroductionValidationResponse.from(violations.isEmpty(), violations);
    }
}
