package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductImage;

/**
 * 상품 이미지 write 포트.
 *
 * <p>화면 갤러리용 이미지 목록 조회는 {@code ProductQueryDao#findProductImagePaths}가 담당한다.
 * {@link #findRepresentativeImageFilePath}는 주문 생성 시 주문 라인에 <b>스냅샷으로 기록</b>하는
 * 값(주문 당시 상품 이미지)이라 command 경로에서 소비되므로 이 포트에 남는다 — 표현 목적 조회가 아니다.
 */
public interface ProductImageRepository {

    /**
     * 상품의 대표 이미지(노출 중 최소 sort) 파일 경로. 없으면 null.
     */
    String findRepresentativeImageFilePath(Long productId);

    ProductImage save(ProductImage productImage);
}
