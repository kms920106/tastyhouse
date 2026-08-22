package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;

/**
 * 검증을 통과한 주문 라인 한 건의 스냅샷.
 *
 * <p>상품 존재·판매중지 검증과 옵션 존재 검증을 마친 뒤, 주문 라인이 <b>주문 당시 값으로 박제</b>해야
 * 하는 것들(상품명·가격명·대표 이미지·정가·할인가·옵션 스냅샷)을 함께 나른다. 이 record가 있으면
 * {@code OrderPlacementService}는 product의 애그리거트·리포지토리를 전혀 몰라도 주문 라인을 만들 수 있다.
 *
 * <p><b>금액 계산은 여기서 하지 않는다</b> — 라인 합계·할인액 산출은 주문 금액 불변식이므로 order
 * 컨텍스트가 소유한다. 이 스냅샷은 그 계산의 <b>입력</b>(단가·추가금)만 제공한다.
 *
 * <p><b>{@code originalPrice}는 주문유형에 따라 이미 해석된 채널 가격이다.</b> 배달·테이블·예약이면
 * 배달가, 포장이면 픽업가(미설정이면 배달가)가 담긴다 — 해석은 {@code ProductPrice#resolvePrice}가
 * 수행하며 클라이언트는 개입하지 않는다. 필드명이 {@code originalPrice}인 것은 order 컨텍스트의 기존
 * 금액 계산식과 저장 컬럼을 그대로 유지하기 위함이다(이 값이 그 라인의 "정가" 역할을 한다).
 *
 * <p>{@code priceName}은 주문 시점의 가격명을 박제한다 — 나중에 점주가 가격명을 바꿔도 과거 주문
 * 전표가 변하지 않아야 한다(기존 이름·가격 박제 원칙과 동일). 가격이 하나뿐인 메뉴는 {@code null}이다.
 *
 * <p>{@code discountPrice}는 할인 미적용 시 {@code null}이다(정가 판매). {@code representativeImageFileId}도
 * 등록된 대표 이미지가 없으면 {@code null}이다.
 */
public record OrderProductSnapshot(
    ProductId productId,
    ProductPriceId productPriceId,
    String name,
    String priceName,
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
