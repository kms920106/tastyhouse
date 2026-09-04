package com.tastyhouse.webapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 상품 배치 조회 입력.
 *
 * <p>경계 타입만 싣는다 — 주문유형은 도메인 enum({@code OrderMethod}) 후보이지만 {@code String}으로
 * 받고 승격은 서비스가 담당한다.
 *
 * <p>인바운드 포트가 {@code ..request..}의 Request record를 직접 받으면 매핑 책임이 어댑터에서
 * application으로 흘러들어 완전 매핑 전략이 깨지므로(그리고 모듈 분리 후에는 컴파일 자체가 불가능하다),
 * 컨트롤러가 {@code Request.toQuery()}로 이 타입을 조립해 넘긴다.
 */
public record ProductBatchQuery(
    List<Item> items,
    String orderMethod
) {

    public ProductBatchQuery {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        items = List.copyOf(items);
    }

    /**
     * 조회 항목 — 상품 식별자와 선택 옵션 식별자(옵션이 없으면 {@code null}).
     */
    public record Item(Long productId, Long optionId) {

        public Item {
            if (productId == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }
    }
}
