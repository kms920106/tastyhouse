package com.tastyhouse.batchapplication.crawling.bbq.port.out;

import java.util.List;

import com.tastyhouse.batchapplication.crawling.bbq.response.BbqProductCategoryResponse;
import com.tastyhouse.batchapplication.crawling.bbq.response.BbqProductResponse;
import com.tastyhouse.batchapplication.crawling.bbq.response.BbqProductSubOptionResponse;

/**
 * BBQ 메뉴 원천 데이터를 외부에서 읽어오는 아웃바운드 포트.
 *
 * <p>구현은 external-api의 {@code BbqMenuAdapter}다. 소비 앱이 batch 하나뿐이므로 읽기 계약
 * 소유 규칙(소비 앱 수가 소유 모듈을 정한다)에 따라 batch-application이 이 계약을 소유하고,
 * 어댑터가 여기에 의존한다 — 의존 역전.
 *
 * <p><b>반환 타입이 이 모듈의 {@code response} 레코드인 이유</b>: 이전에는 {@code BbqService}가
 * external-api의 wire DTO({@code BbqMenuResponse} 등, {@code @JsonProperty}로 BBQ API 응답
 * 스키마에 결박된 타입)를 직접 받아 변환했다. 그러면 BBQ가 응답 필드명을 바꿀 때 파장이
 * application까지 올라온다. 변환을 어댑터로 내리면 wire DTO는 external-api 내부 협력 타입으로
 * 갇히고, 이 포트의 시그니처는 프레임워크-프리로 유지된다.
 */
public interface BbqMenuPort {

    List<BbqProductCategoryResponse> fetchMenuCategories();

    List<BbqProductResponse> fetchMenusByCategoryId(Long categoryId);

    BbqProductResponse fetchMenuDetail(Long menuId);

    List<BbqProductSubOptionResponse> fetchMenuSubOptions(Long menuId);
}
