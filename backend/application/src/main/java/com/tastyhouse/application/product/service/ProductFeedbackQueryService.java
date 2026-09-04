package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductFeedbackQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.ProductFeedbackQueryPort;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;

/**
 * 점주용 고객 의견 조회 서비스(CQRS query 측).
 *
 * <p>조회 범위(지난 7일)의 기준 상수는 도메인({@link ProductFeedbackService#FEEDBACK_WINDOW_DAYS})이
 * 소유한다 — 목록의 창과 중복 제보 방지 창이 어긋나면 창 밖 재제보로 집계가 부풀기 때문에, 두 값이
 * 같은 상수 하나를 공유해야 한다.
 *
 * <p>미확인 판정({@code getUnread})은 DAO가 아니라 도메인 서비스를 경유한다 — 확인 시각과 제보 접수
 * 시각을 함께 봐야 하는 판정이라 조회 투영이 아니라 규칙이다.
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductFeedbackQueryService implements ProductFeedbackQueryUseCase {

    private final ProductFeedbackQueryPort productFeedbackQueryPort;
    private final ProductFeedbackService productFeedbackService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackQueryService(
        ProductFeedbackQueryPort productFeedbackQueryPort,
        ProductFeedbackService productFeedbackService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productFeedbackQueryPort = productFeedbackQueryPort;
        this.productFeedbackService = productFeedbackService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 가게의 지난 한 주 고객 의견을 메뉴 × 유형 집계로 조회한다.
     */
    @Override
    public PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime since = LocalDateTime.now().minusDays(ProductFeedbackService.FEEDBACK_WINDOW_DAYS);
        return productFeedbackQueryPort.findFeedbackSummaries(
            shopId, since, PageQuery.of(page, size)
        );
    }

    /**
     * 확인하지 않은 의견이 있는지 — 화면 아이콘의 빨간 점 판정.
     */
    @Override
    public boolean getUnread(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productFeedbackService.hasUnread(ShopId.of(shopId), LocalDateTime.now());
    }

}
