package com.tastyhouse.domain.product.repository;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 정보 고객 의견 write 포트.
 *
 * <p>{@code existsRecentDuplicate}가 여기 있는 이유: 중복 제보 판정은 표현 목적 조회가 아니라
 * <b>쓰기 불변식</b>(같은 회원·같은 메뉴·같은 유형 7일 내 1회)이다. 목록·집계 조회는 이 포트가 아니라
 * infrastructure-module의 {@code ProductFeedbackQueryDao}가 소유한다.
 */
public interface ProductFeedbackRepository {

    ProductFeedback save(ProductFeedback feedback);

    /**
     * 같은 회원이 같은 메뉴에 같은 유형으로 {@code since} 이후에 이미 제보했는지.
     * 기준 시각을 파라미터로 받아 도메인이 시계를 직접 읽지 않게 한다.
     */
    boolean existsRecentDuplicate(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    );

    /** 이 가게에 {@code since} 이후 접수된 제보가 하나라도 있는지 — 빨간 점 판정. */
    boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since);
}
