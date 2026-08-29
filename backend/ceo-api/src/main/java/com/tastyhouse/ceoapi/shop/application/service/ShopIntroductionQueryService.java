package com.tastyhouse.ceoapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.shop.query.ShopOwnerMessageResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopIntroductionResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopIntroductionValidationResponse;

/**
 * 점주용 가게소개(사장님 한마디) 조회·사전검증 서비스(CQRS query 측).
 *
 * <p>사전검증({@link #validateIntroduction})은 저장 없이 금칙어 위반 목록만 돌려주는 읽기 연산이므로
 * query 측에 둔다. 최신 사장님 한마디는 표현 목적 조회이므로 infra query DAO에서 Result를 받아 조립한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopIntroductionQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionQueryService(
        ShopQueryDao shopQueryDao,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopQueryDao = shopQueryDao;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ShopIntroductionResponse getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String message = shopQueryDao.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageResult::message)
            .orElse(null);
        return ShopIntroductionResponse.from(message);
    }

    public ShopIntroductionValidationResponse validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return ShopIntroductionValidationResponse.from(violations.isEmpty(), violations);
    }
}
