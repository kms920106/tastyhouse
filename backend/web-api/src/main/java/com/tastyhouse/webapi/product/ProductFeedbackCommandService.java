package com.tastyhouse.webapi.product;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 손님의 메뉴 정보 의견 제보 서비스(CQRS command 측).
 *
 * <p>불변식은 전부 도메인({@link ProductFeedbackService})이 소유한다 — 메뉴 존재 확인, 제보 가게 결정,
 * 중복 제보 방지, 내용 필수·길이까지. 이 서비스는 트랜잭션 경계·VO 승격·문자열 → enum 승격만 담당한다.
 *
 * <p><b>가게를 요청에서 받지 않는다.</b> 클라이언트가 실어 보낸 가게를 믿으면 남의 가게 목록에 제보를
 * 꽂아 넣을 수 있으므로, 도메인이 메뉴에서 끌어온다.
 *
 * <p>{@code now}를 여기서 만들어 넘긴다 — 도메인이 시계를 직접 읽으면 중복 판정 창을 테스트에서
 * 고정할 수 없다.
 */
@Service
@Transactional
public class ProductFeedbackCommandService {

    private final ProductFeedbackService productFeedbackService;

    public ProductFeedbackCommandService(ProductFeedbackService productFeedbackService) {
        this.productFeedbackService = productFeedbackService;
    }

    /**
     * 메뉴 정보에 대한 의견을 접수하고 생성된 id를 반환한다.
     */
    public Long submitFeedback(Long memberId, Long productId, String feedbackType, String content) {
        MemberId reporterId = MemberId.of(memberId);
        ProductId targetProductId = ProductId.of(productId);
        ProductFeedbackType type = ProductFeedbackType.from(feedbackType);

        return productFeedbackService.submit(reporterId, targetProductId, type, content, LocalDateTime.now())
            .getId();
    }
}
