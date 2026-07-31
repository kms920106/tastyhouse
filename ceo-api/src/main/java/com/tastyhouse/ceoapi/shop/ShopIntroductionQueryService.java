package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.domain.service.ProhibitedWordValidator;
import com.tastyhouse.ceoapi.shop.response.ShopIntroductionResponse;
import com.tastyhouse.ceoapi.shop.response.ShopIntroductionValidationResponse;

/**
 * 점주용 가게소개(사장님 한마디) 조회·사전검증 서비스(CQRS query 측).
 *
 * <p>사전검증({@link #validateIntroduction})은 저장 없이 금칙어 위반 목록만 돌려주는 읽기 연산이므로
 * query 측에 둔다. 최신 사장님 한마디 조회는 write 포트에 잔류한 단건 조회를 쓴다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopIntroductionQueryService {

    private final ShopDetailRepository shopDetailRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionResponse getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String message = shopDetailRepository.findLatestOwnerMessageByShopId(shopId)
            .map(ShopOwnerMessageHistory::getMessage)
            .orElse(null);
        return ShopIntroductionResponse.from(message);
    }

    public ShopIntroductionValidationResponse validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return ShopIntroductionValidationResponse.from(violations.isEmpty(), violations);
    }
}
