package com.tastyhouse.infrastructure.product.query;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductFeedbackType;

/**
 * 점주 화면의 고객 의견 한 줄 — 메뉴 × 유형으로 묶은 지난 한 주 집계.
 *
 * <p><b>제보 건별이 아니라 집계인 것이 이 read model의 핵심</b>이다. 점주가 알아야 하는 것은 "누가
 * 언제 보냈는가"가 아니라 "어떤 메뉴의 무엇이 몇 명에게 틀려 보이는가"다. 건별로 내려보내면 같은
 * 지적이 수십 줄로 흩어져 무엇을 고쳐야 할지 판단할 수 없다.
 *
 * <p><b>제보자 정보(회원 ID·닉네임)를 담지 않는다.</b> 점주가 특정 손님을 식별하면 보복 우려가 있고,
 * 제보의 목적은 정보 수정이지 손님 응대가 아니다. 이 필드가 read model 단계부터 없어야 응답 조립에서
 * 실수로 흘리는 경로가 만들어지지 않는다.
 *
 * <p>{@code contents}는 {@code ETC} 유형의 서술만 담으며 최대 10건이다 — 그 외 유형은 유형 자체가
 * 내용이라 서술이 없고, 무제한으로 실으면 한 메뉴의 제보가 응답을 뒤덮는다.
 */
public record ProductFeedbackSummaryResult(
    Long productId,
    String productName,
    ProductFeedbackType feedbackType,
    Integer count,
    List<String> contents
) {
}
