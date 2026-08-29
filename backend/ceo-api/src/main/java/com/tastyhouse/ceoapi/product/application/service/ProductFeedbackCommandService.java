package com.tastyhouse.ceoapi.product.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.product.application.port.in.ProductFeedbackCommandUseCase;
import com.tastyhouse.ceoapi.product.application.port.in.ProductFeedbackReadCommand;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주용 고객 의견 확인 처리 서비스(CQRS command 측).
 *
 * <p>목록을 연 시점에 호출해 빨간 점을 끈다. 확인 시각을 밀어 올리는 것이 전부이며, 제보 자체는
 * 수정·삭제되지 않는다 — 반복 제보 추이가 근거 자료가 되기 때문이다.
 */
@Service
@Transactional
public class ProductFeedbackCommandService implements ProductFeedbackCommandUseCase {

    private final ProductFeedbackService productFeedbackService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackCommandService(
        ProductFeedbackService productFeedbackService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productFeedbackService = productFeedbackService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 고객 의견을 확인 처리한다(빨간 점 끄기).
     */
    @Override
    public void markRead(ProductFeedbackReadCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        productFeedbackService.markRead(ShopId.of(shopId), LocalDateTime.now());
    }
}
