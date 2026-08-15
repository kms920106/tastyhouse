package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;

/**
 * 검증을 통과한 주문 라인 한 건의 스냅샷.
 *
 * <p>상품 존재·판매중지 검증과 옵션 존재 검증을 마친 뒤, 주문 라인이 <b>주문 당시 값으로 박제</b>해야
 * 하는 것들(상품명·대표 이미지·정가·할인가·옵션 스냅샷)을 함께 나른다. 이 record가 있으면
 * {@code OrderPlacementService}는 product의 애그리거트·리포지토리를 전혀 몰라도 주문 라인을 만들 수 있다.
 *
 * <p><b>금액 계산은 여기서 하지 않는다</b> — 라인 합계·할인액 산출은 주문 금액 불변식이므로 order
 * 컨텍스트가 소유한다. 이 스냅샷은 그 계산의 <b>입력</b>(단가·추가금)만 제공한다.
 *
 * <p>{@code discountPrice}는 할인 미적용 시 {@code null}이다(정가 판매). {@code representativeImageFileId}도
 * 등록된 대표 이미지가 없으면 {@code null}이다.
 */
public record OrderProductSnapshot(
    ProductId productId,
    String name,
    UploadedFileId representativeImageFileId,
    int quantity,
    int originalPrice,
    Integer discountPrice,
    List<OrderProductOptionSnapshot> options
) {

    public OrderProductSnapshot {
        options = options == null ? List.of() : List.copyOf(options);
    }

    /**
     * 할인가가 있으면 할인가, 없으면 정가 — 라인 금액 계산의 단가 기준이다.
     */
    public int effectivePrice() {
        return this.discountPrice != null ? this.discountPrice : this.originalPrice;
    }
}
